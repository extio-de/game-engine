package de.extio.game_engine.audio;

import java.util.ArrayList;
import java.util.List;

import de.extio.game_engine.resource.StaticResource;
import de.extio.game_engine.resource.StaticResourceService;
import de.extio.game_engine.storage.StorageService;

public final class AudioController implements AudioControl {
	
	private final StorageService storageService;

	private final AudioOptions audioOptions; // Passed as reference to strategies
	
	private AudioStrategy audioStrategy;
	
	private final AudioLoader audioLoader;
	
	private Thread audioLoaderThread;
	
	private final Thread audioControllerThread;
	
	private final List<StaticResource> standardMusic = new ArrayList<>();
	
	private List<StaticResource> lastMusicQueued = null;
	
	private boolean started;
	
	private boolean useSoftwareMixing;
	
	public AudioController(final StaticResourceService resourceService, final StorageService storageService) {
		this.storageService = storageService;
		this.audioOptions = new AudioOptions();
		storageService.loadByPath(AudioOptions.class, List.of("gameEngine"), "audioOptions").ifPresentOrElse(
				loadedOptions -> {
					this.audioOptions.apply(loadedOptions);
				},
				() -> {
					this.storeAudioOptions();
				});
		
		this.audioLoader = new AudioLoader(resourceService);
		
		this.startStrategy();
		
		this.audioControllerThread = Thread.ofPlatform()
				.name("Audio Controller")
				.daemon(true)
				.start(() -> {
					while (!Thread.currentThread().isInterrupted()) {
						try {
							Thread.sleep(100);
						}
						catch (final InterruptedException e) {
							Thread.currentThread().interrupt();
							break;
						}
						
						this.run();
					}
				});
	}

	private void storeAudioOptions() {
		this.storageService.store(List.of("gameEngine"), "audioOptions", this.audioOptions);
	}
	
	public void run() {
		if (!this.started || this.audioOptions.getSfxOptions().isSoftwareMixing() != this.useSoftwareMixing) {
			this.stopStrategy();
			this.startStrategy();
		}
		
		if (this.audioStrategy != null) {
			this.audioStrategy.run();
		}
	}
	
	public void shutdown() {
		this.audioControllerThread.interrupt();
		this.stopStrategy();
	}
	
	private void startStrategy() {
		if (!this.started) {
			this.audioLoader.getQueue().clear();
			this.audioLoaderThread = Thread.ofPlatform()
					.name("Audio Loader")
					.daemon(true)
					.start(this.audioLoader);
			
			if (this.audioOptions.getSfxOptions().isSoftwareMixing()) {
				this.audioStrategy = new AudioStrategySoftwareMixing(this, this.audioOptions, this.audioLoader);
				this.useSoftwareMixing = true;
			}
			else {
				this.audioStrategy = new AudioStrategyJavaMixer(this, this.audioOptions, this.audioLoader);
				this.useSoftwareMixing = false;
			}
			
			this.audioStrategy.start();
			if (this.lastMusicQueued != null) {
				this.audioStrategy.queueMusic(this.lastMusicQueued);
			}
			
			this.started = true;
		}
	}
	
	private void stopStrategy() {
		if (this.started) {
			if (this.audioStrategy != null) {
				this.audioStrategy.stop();
				this.audioStrategy = null;
			}
			if (this.audioLoaderThread != null) {
				this.audioLoaderThread.interrupt();
				this.audioLoaderThread = null;
			}
			this.audioLoader.getQueue().clear();
			this.started = false;
		}
	}
	
	@Override
	public void playMusic(final StaticResource opener, final List<StaticResource> audioResources, final boolean standard) {
		if (audioResources == null) {
			return;
		}
		
		if (standard) {
			this.standardMusic.clear();
			this.standardMusic.addAll(audioResources);
		}
		
		if (this.audioStrategy != null) {
			this.lastMusicQueued = this.wrapOpener(opener, audioResources);
			this.audioStrategy.queueMusic(this.lastMusicQueued);
		}
	}
	
	@Override
	public void playStandardMusic() {
		if (this.audioStrategy != null) {
			this.lastMusicQueued = this.wrapOpener(null, this.standardMusic);
			this.audioStrategy.queueMusic(this.lastMusicQueued);
		}
	}
	
	@Override
	public void stopMusic() {
		if (this.audioStrategy != null) {
			this.audioStrategy.clearMusic();
		}
		this.lastMusicQueued = null;
	}
	
	@Override
	public void play(final StaticResource audioResource) {
		if (this.audioStrategy != null) {
			this.audioStrategy.queueSfx(List.of(audioResource));
		}
	}
	
	@Override
	public AudioOptions getAudioOptions() {
		return new AudioOptions(this.audioOptions);
	}
	
	@Override
	public void applyAudioOptions(final AudioOptions audioOptions) {
		this.audioOptions.apply(audioOptions);
		this.storeAudioOptions();
	}
	
	private List<StaticResource> wrapOpener(final StaticResource opener, final List<StaticResource> audioFileNames) {
		final List<StaticResource> result = new ArrayList<>(audioFileNames.size() + 1);
		result.add(opener);
		result.addAll(audioFileNames);
		return result;
	}
	
}
