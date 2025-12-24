package de.extio.game_engine.audio;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.util.rng.XorShift128Random;

final class AudioStrategySoftwareMixing implements AudioStrategy {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(AudioStrategySoftwareMixing.class);
	
	private final static int MUSIC_INITIAL_DELAY_MS = 1000;
	
	private final AudioControl audioControl;

	private final AudioOptions audioOptions;
	
	private final AudioLoader audioLoader;
	
	private final Random random = new XorShift128Random();
	
	private AudioThread audioPlayer;
	
	private final Set<StaticResource> sfxQueue = Collections.synchronizedSet(new LinkedHashSet<>());
	
	private final List<StaticResource> musicFiles = new ArrayList<>();
	
	private final Deque<StaticResource> musicPlayQueue = new ArrayDeque<>();
	
	private long musicFirstQueued;
	
	public AudioStrategySoftwareMixing(final AudioControl audioControl, final AudioOptions audioOptions, final AudioLoader audioLoader) {
		this.audioControl = audioControl;
		this.audioOptions = audioOptions;
		this.audioLoader = audioLoader;
	}
	
	@Override
	public void start() {
		final var multiStreamPlayer = new AudioPlayerMultiStream(this.audioControl);
		final var multiStreamThread = Thread.ofPlatform()
				.name("Audio MultiStream")
				.daemon(true)
				.start(multiStreamPlayer);
		this.audioPlayer = new AudioThread(multiStreamThread, multiStreamPlayer);
	}
	
	@Override
	public void stop() {
		this.audioPlayer.audioPlayer().exit();
		this.audioPlayer.thread().interrupt();
	}
	
	@Override
	public void run() {
		if (this.audioPlayer.audioPlayer().isPlaying() || !this.sfxQueue.isEmpty() || !this.musicPlayQueue.isEmpty() || !this.musicFiles.isEmpty()) {
			this.audioPlayer.audioPlayer().updateAudioOptions(this.audioOptions.getSfxOptions());
			this.audioPlayer.audioPlayer().updateMusicAudioOptions(this.audioOptions.getMusicOptions());
		}
		
		// Music
		
		if (!this.audioOptions.getMusicOptions().isEnable()) {
			if (this.audioPlayer.audioPlayer().isMusicPlaying()) {
				this.audioPlayer.audioPlayer().setStopMusic(true);
			}
		}
		else {
			if (this.musicPlayQueue.isEmpty() && !this.musicFiles.isEmpty()) {
				final List<StaticResource> music = new ArrayList<>(this.musicFiles);
				Collections.shuffle(music, this.random);
				this.musicPlayQueue.addAll(music);
				
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Music play queue: " + this.musicPlayQueue);
				}
			}
			if (!this.musicPlayQueue.isEmpty() && this.audioPlayer.audioPlayer().getMusicQueue() == null && !this.audioPlayer.audioPlayer().isMusicPlaying() && (System.currentTimeMillis() - this.musicFirstQueued) >= MUSIC_INITIAL_DELAY_MS) {
				final StaticResource nextFile = this.musicPlayQueue.pop();
				this.musicFirstQueued = System.currentTimeMillis();
				this.audioLoader.getQueue().offer(new AudioLoaderRequest(nextFile, false, stream -> this.audioPlayer.audioPlayer().setMusicQueue(new AudioData(nextFile.resourceName(), stream))));
			}
		}
		
		// Sfx
		
		synchronized (this.sfxQueue) {
			if (!this.sfxQueue.isEmpty()) {
				final Iterator<StaticResource> it = this.sfxQueue.iterator();
				while (it.hasNext()) {
					final StaticResource nextFile = it.next();
					this.audioLoader.getQueue().offer(new AudioLoaderRequest(nextFile, true, stream -> this.audioPlayer.audioPlayer().getQueue().offer(new AudioData(nextFile.resourceName(), stream))));
					it.remove();
				}
			}
		}
	}
	
	@Override
	public void clearSfx() {
		this.sfxQueue.clear();
	}
	
	@Override
	public void queueSfx(final Collection<StaticResource> audioFiles) {
		if (!this.audioOptions.getSfxOptions().isEnable()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Not queued audio file (audio disabled): " + audioFiles);
			}
		}
		else if (this.sfxQueue.addAll(audioFiles)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Queued audio file: " + audioFiles);
			}
		}
		else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Not queued audio file (already queued): " + audioFiles);
			}
		}
	}
	
	@Override
	public void queueMusic(final Collection<StaticResource> audioFiles) {
		Optional<StaticResource> opener = null;
		final List<StaticResource> musicAudioFiles = new ArrayList<>(Math.min(1, audioFiles.size() - 1));
		for (final StaticResource audioFile : audioFiles) {
			if (opener == null) {
				opener = Optional.ofNullable(audioFile);
			}
			else {
				musicAudioFiles.add(audioFile);
			}
		}
		
		if (musicAudioFiles.equals(this.musicFiles)) {
			this.musicFirstQueued = System.currentTimeMillis();
		}
		
		this.clearMusic();
		if (opener != null && opener.isPresent()) {
			this.musicPlayQueue.offer(opener.get());
		}
		this.musicFiles.addAll(musicAudioFiles);
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Queued music: Opener: " + (opener != null ? opener.orElse(null) : null) + " Files: " + this.musicFiles);
		}
	}
	
	@Override
	public void clearMusic() {
		this.musicFiles.clear();
		this.musicPlayQueue.clear();
		
		this.audioPlayer.audioPlayer().setStopMusic(true);
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Stopped and cleared music");
		}
	}
	
	private static record AudioThread(Thread thread, AudioPlayerMultiStream audioPlayer) {
	}
	
}
