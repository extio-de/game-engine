package de.extio.game_engine.audio;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.util.rng.XorShift128Random;

final class AudioStrategyJavaMixer implements AudioStrategy {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(AudioStrategyJavaMixer.class);
	
	private final static int SFX_PLAYERS = 3;
	
	private final static int MUSIC_INITIAL_DELAY_MS = 1000;
	
	private final AudioControl audioControl;

	private final AudioOptions audioOptions;
	
	private final AudioLoader audioLoader;
	
	private final Random random = new XorShift128Random();
	
	private final List<AudioThread> sfxAudioPlayers = new ArrayList<>(SFX_PLAYERS);
	
	private final Set<StaticResource> sfxQueue = Collections.synchronizedSet(new LinkedHashSet<>());
	
	private final List<AudioPlayerReference> sfxPlayersBySize = new ArrayList<>(SFX_PLAYERS);
	
	private AudioThread musicAudioPlayer;
	
	private final List<StaticResource> musicFiles = new ArrayList<>();
	
	private StaticResource musicPlayOpener = null;
	
	private final Deque<StaticResource> musicPlayQueue = new ArrayDeque<>();
	
	private long musicFirstQueued;
	
	public AudioStrategyJavaMixer(final AudioControl audioControl, final AudioOptions audioOptions, final AudioLoader audioLoader) {
		this.audioControl = audioControl;
		this.audioOptions = audioOptions;
		this.audioLoader = audioLoader;
	}
	
	@Override
	public void start() {
		{
			final var musicAudioPlayer = new AudioPlayerSingleStream(this.audioControl);
			final var musicThread = Thread.ofPlatform()
					.name("Audio Music")
					.daemon(true)
					.start(musicAudioPlayer);
			musicThread.start();
			this.musicAudioPlayer = new AudioThread(musicThread, musicAudioPlayer);
		}
		
		for (int i = 0; i < SFX_PLAYERS; i++) {
			final AudioPlayer audioPlayer = new AudioPlayerSingleStream(this.audioControl);
			final var audioThread = Thread.ofPlatform()
					.name("Audio SFX " + i)
					.daemon(true)
					.start(audioPlayer);
			
			this.sfxAudioPlayers.add(new AudioThread(audioThread, audioPlayer));
			this.sfxPlayersBySize.add(new AudioPlayerReference(audioPlayer));
		}
	}
	
	@Override
	public void stop() {
		this.musicAudioPlayer.audioPlayer().exit();
		this.musicAudioPlayer.thread().interrupt();
		
		for (int i = 0; i < SFX_PLAYERS; i++) {
			this.sfxAudioPlayers.get(i).audioPlayer.exit();
			this.sfxAudioPlayers.get(i).thread.interrupt();
		}
	}
	
	@Override
	public void run() {
		// Music
		
		if (!this.audioOptions.getMusicOptions().isEnable()) {
			if (this.musicAudioPlayer.audioPlayer().isPlaying()) {
				this.musicAudioPlayer.audioPlayer().stopAudio();
			}
		}
		else {
			if (this.musicPlayQueue.isEmpty() && (this.musicPlayOpener != null || !this.musicFiles.isEmpty())) {
				if (this.musicPlayOpener != null) {
					this.musicPlayQueue.add(this.musicPlayOpener);
					this.musicPlayOpener = null;
				}
				
				final List<StaticResource> music = new ArrayList<>(this.musicFiles);
				Collections.shuffle(music, this.random);
				this.musicPlayQueue.addAll(music);
				
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Music play queue: " + this.musicPlayQueue);
				}
			}
			
			if (this.musicAudioPlayer.audioPlayer().isPlaying() || !this.musicPlayQueue.isEmpty()) {
				this.musicAudioPlayer.audioPlayer().updateAudioOptions(this.audioOptions.getMusicOptions());
			}
			
			if (!this.musicAudioPlayer.audioPlayer().isPlaying() &&
					!this.musicPlayQueue.isEmpty() &&
					System.currentTimeMillis() - this.musicFirstQueued >= MUSIC_INITIAL_DELAY_MS) {
				final StaticResource nextFile = this.musicPlayQueue.pop();
				
				this.audioLoader.getQueue().offer(new AudioLoaderRequest(nextFile, false, stream -> this.musicAudioPlayer.audioPlayer().getQueue().offer(new AudioData(nextFile.resourceName(), stream))));
			}
		}
		
		// SFX
		
		synchronized (this.sfxQueue) {
			if (!this.sfxQueue.isEmpty()) {
				for (int i = 0; i < SFX_PLAYERS; i++) {
					this.sfxAudioPlayers.get(i).audioPlayer().updateAudioOptions(this.audioOptions.getSfxOptions());
					this.sfxPlayersBySize.get(i).queueSize = this.sfxPlayersBySize.get(i).audioPlayer.getQueue().size();
				}
				Collections.sort(this.sfxPlayersBySize);
				
				while (!this.sfxQueue.isEmpty()) {
					int i = 0;
					final Iterator<StaticResource> it = this.sfxQueue.iterator();
					do {
						this.sfxPlayersBySize.get(i).queueSize++;
						
						final AudioPlayer player = this.sfxPlayersBySize.get(i).audioPlayer;
						final StaticResource nextFile = it.next();
						it.remove();
						
						this.audioLoader.getQueue().offer(new AudioLoaderRequest(nextFile, true, stream -> player.getQueue().offer(new AudioData(nextFile.resourceName(), stream))));
					} while (it.hasNext() && ++i < this.sfxPlayersBySize.size() && this.sfxPlayersBySize.get(i - 1).queueSize > this.sfxPlayersBySize.get(i).queueSize);
				}
			}
		}
	}
	
	@Override
	public void queueSfx(final Collection<StaticResource> audioFile) {
		if (!this.audioOptions.getSfxOptions().isEnable()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Not queued audio file (audio disabled): " + audioFile);
			}
		}
		else if (this.sfxQueue.addAll(audioFile)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Queued audio file: " + audioFile);
			}
		}
		else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Not queued audio file (already queued): " + audioFile);
			}
		}
	}
	
	@Override
	public void clearSfx() {
		this.sfxQueue.clear();
	}
	
	@Override
	public void queueMusic(final Collection<StaticResource> audioFiles) {
		StaticResource opener = null;
		final List<StaticResource> musicAudioFiles = new ArrayList<>(audioFiles.size() - 1);
		boolean first = true;
		for (final StaticResource audioFile : audioFiles) {
			if (first) {
				opener = audioFile;
				first = false;
			}
			else {
				musicAudioFiles.add(audioFile);
			}
		}
		
		if (musicAudioFiles.equals(this.musicFiles)) {
			LOGGER.debug("The selected tracks are already running");
			return;
		}
		
		if (this.musicAudioPlayer.audioPlayer().isPlaying()) {
			this.musicFirstQueued = System.currentTimeMillis();
		}
		
		this.clearMusic();
		this.musicPlayOpener = opener;
		this.musicFiles.addAll(musicAudioFiles);
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Queued music: " + this.musicFiles);
		}
	}
	
	@Override
	public void clearMusic() {
		this.musicFiles.clear();
		this.musicPlayQueue.clear();
		this.musicPlayOpener = null;
		
		this.musicAudioPlayer.audioPlayer().stopAudio();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Stopped and cleared music player");
		}
	}
	
	static class AudioPlayerReference implements Comparable<AudioPlayerReference> {
		
		private int queueSize;
		
		private final AudioPlayer audioPlayer;
		
		AudioPlayerReference(final AudioPlayer audioPlayer) {
			this.audioPlayer = audioPlayer;
		}
		
		@Override
		public int compareTo(final AudioPlayerReference o) {
			return Integer.compare(this.queueSize, o.queueSize);
		}
		
	}
	
	private static record AudioThread(Thread thread, AudioPlayer audioPlayer) {
	}
	
}
