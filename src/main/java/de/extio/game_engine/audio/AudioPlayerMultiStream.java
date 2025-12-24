package de.extio.game_engine.audio;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.extio.game_engine.audio.AudioOptions.AudioStreamOptions;

final class AudioPlayerMultiStream implements AudioPlayer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AudioPlayerMultiStream.class);
	
	private static final int STREAMS = 4;
	
	private static final int MUSIC_FADING_MS = 1000;
	
	private final BlockingQueue<AudioData> queue = new LinkedBlockingQueue<>(20);
	
	private volatile AudioData musicQueue;
	
	private volatile boolean musicPlaying;
	
	private volatile boolean stopAudio;
	
	private volatile boolean stopMusic;
	
	private volatile boolean playing;
	
	private volatile double sfxVolume;
	
	private volatile boolean sfxMute;
	
	private volatile double musicVolume;
	
	private volatile boolean musicMute;
	
	private volatile boolean exit;
	
	private boolean curMute;
	
	private long notStartedAudioCnt;
	
	public AudioPlayerMultiStream() {
	}
	
	@Override
	public void run() {
		LOGGER.debug("start");
		
		final int[] mix = new int[STREAMS];
		final double[] vols = new double[STREAMS];
		double musicFadeVol = 1.0;
		long musicFading = 0;
		final AudioBuffer[] buffers = new AudioBuffer[STREAMS];
		final AudioPlayerStream[] streams = new AudioPlayerStream[STREAMS];
		for (int i = 0; i < STREAMS; i++) {
			buffers[i] = new AudioBuffer();
			streams[i] = new AudioPlayerStream();
		}
		final AudioBuffer mixBuffer = new AudioBuffer();
		
		while (!Thread.currentThread().isInterrupted() && !this.exit) {
			SourceDataLine line = null;
			try {
				while (!Thread.currentThread().isInterrupted() && !this.exit) {
					if (this.stopAudio) {
						for (int i = 0; i < STREAMS; i++) {
							try {
								streams[i].close();
							}
							catch (final Exception e) {
								LOGGER.error(e.getMessage(), e);
							}
						}
						this.musicPlaying = false;
						this.stopAudio = false;
					}
					if (this.stopMusic) {
						if (this.musicPlaying && musicFading == 0) {
							musicFading = System.currentTimeMillis();
						}
						this.stopMusic = false;
					}
					if (musicFading > 0) {
						final int ms = (int) (System.currentTimeMillis() - musicFading);
						if (ms >= MUSIC_FADING_MS) {
							streams[0].close();
							this.musicPlaying = false;
							musicFadeVol = 1.0;
							musicFading = 0;
						}
						else {
							musicFadeVol = 1.0 - ((double) ms / MUSIC_FADING_MS);
						}
					}
					else {
						musicFadeVol = 1.0;
					}
					
					// Load music
					{
						if (streams[0].inStream == null) {
							final AudioData musicQueued = this.musicQueue;
							if (musicQueued != null) {
								if (this.setupStream(musicQueued, streams, 0)) {
									this.musicPlaying = true;
								}
								this.musicQueue = null;
							}
						}
					}
					
					// Load new sfx streams if available
					for (int i = 1; i < STREAMS; i++) {
						if (streams[i].inStream == null) {
							final AudioData data = this.queue.poll();
							if (data == null) {
								break;
							}
							else {
								this.setupStream(data, streams, i);
							}
						}
					}
					
					// Manage streams
					
					int streamsActive = 0;
					for (int i = 0; i < STREAMS; i++) {
						if (streams[i].inStream != null) {
							streamsActive++;
						}
						buffers[i].size = 0;
					}
					
					this.playing = streamsActive > 0;
					if (!this.playing) {
						// No stream active
						
						if (line != null) {
							// Close line
							line.drain();
							line.stop();
							line.close();
							line = null;
							
							LOGGER.debug("Line closed");
						}
						
						Thread.sleep(5);
						continue;
					}
					
					if (line == null) {
						// Init line
						for (int i = 0; i < STREAMS; i++) {
							if (streams[i].info != null) {
								line = (SourceDataLine) AudioSystem.getLine(streams[i].info);
								if (line != null) {
									line.open(streams[i].info.getFormats()[0]);
									line.start();
									LOGGER.debug("Line opened");
									this.applyOptions(line, true);
								}
								break;
							}
						}
					}
					if (line == null) {
						Thread.sleep(5);
						continue;
					}
					
					this.applyOptions(line, false);
					
					// Read stream buffers
					
					boolean readMore;
					do {
						readMore = false;
						for (int i = 0; i < STREAMS; i++) {
							if (streams[i].convertedAudio != null && buffers[i].size < buffers[i].bytes.length) {
								final int read = streams[i].convertedAudio.read(buffers[i].bytes, buffers[i].size, buffers[i].bytes.length - buffers[i].size);
								if (read == -1) {
									streams[i].close();
									if (i == 0) {
										this.musicPlaying = false;
									}
									
									if (LOGGER.isDebugEnabled()) {
										LOGGER.debug("End of stream " + i);
									}
								}
								else {
									buffers[i].size += read;
									readMore |= buffers[i].size < buffers[i].bytes.length;
									
									//if (LOGGER.isDebugEnabled()) {
									//	LOGGER.debug("Read buffer " + i + " " + buffers[i].size);
									//}
								}
							}
						}
					} while (readMore && !this.exit);
					
					for (int i = 0; i < STREAMS; i++) {
						if (i == 0) {
							vols[i] = this.musicVolume * musicFadeVol;
						}
						else {
							vols[i] = this.sfxVolume;
						}
					}
					
					int val;
					int samplesAvailable;
					mixBuffer.size = mixBuffer.bytes.length;
					for (int n = 0; n < mixBuffer.bytes.length; n += 2) {
						// Collect samples, factor in volume at this point
						samplesAvailable = 0;
						for (int i = 0; i < STREAMS; i++) {
							if (buffers[i].size > n + 1) {
								mix[samplesAvailable++] = (int) (((buffers[i].bytes[n] & 0xFF) | (short) (buffers[i].bytes[n + 1] << 8)) * vols[i]);
							}
						}
						
						// Mix samples
						val = 0;
						if (samplesAvailable == 1) {
							val = mix[0];
						}
						else if (samplesAvailable > 1) {
							val = mix[0] + 32768;
							for (int i = 1; i < samplesAvailable; i++) {
								mix[i] += 32768;
								
								if ((val < 32768) || (mix[i] < 32768)) {
									val = val * mix[i] / 32768;
								}
								else {
									val = 2 * (val + mix[i]) - (val * mix[i]) / 32768 - 65536;
								}
								
								if (val == 65536) {
									val = 65535;
								}
							}
							val -= 32768;
						}
						
						// Write sample
						if (samplesAvailable > 0) {
							mixBuffer.bytes[n] = (byte) (val & 0xFF);
							mixBuffer.bytes[n + 1] = (byte) (val >> 8);
						}
						else {
							mixBuffer.size = n;
							break;
						}
					}
					
					// Write mix buffer
					if (mixBuffer.size > 0) {
						//LOGGER.debug("Write " + mixBuffer.size);
						line.write(mixBuffer.bytes, 0, mixBuffer.size);
					}
					
					this.notStartedAudioCnt = 0;
				}
			}
			catch (final InterruptedException exc) {
				LOGGER.info(exc.getClass().getName());
				break;
			}
			catch (final Exception exc) {
				LOGGER.error(exc.getMessage(), exc);
				
				if (++this.notStartedAudioCnt > 5) {
					this.notStartedAudioCnt = 0;
					this.exit = true;
					LOGGER.error("Too many audio playback errors, shutting down audio player.");
				}
			}
			finally {
				if (line != null) {
					line.close();
					line = null;
					LOGGER.debug("Line closed");
				}
				for (int i = 0; i < STREAMS; i++) {
					try {
						streams[i].close();
					}
					catch (final Exception e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
				this.musicPlaying = false;
			}
		}
		
		this.playing = false;
		LOGGER.debug("end");
	}
	
	private boolean setupStream(final AudioData data, final AudioPlayerStream[] streams, final int idx) throws Exception {
		try {
			streams[idx].inStream = data.inputStream();
			streams[idx].inAudio = AudioSystem.getAudioInputStream(streams[idx].inStream);
			
			final AudioFormat inFormat = streams[idx].inAudio.getFormat();
			final int ch = inFormat.getChannels();
			final float rate = inFormat.getSampleRate();
			final AudioFormat outFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
			streams[idx].info = new Info(SourceDataLine.class, outFormat);
			streams[idx].convertedAudio = AudioSystem.getAudioInputStream(outFormat, streams[idx].inAudio);
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Start stream " + idx + " " + data.name() + ": in " + inFormat + "; out " + outFormat);
			}
			
			return true;
		}
		catch (final Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			streams[idx].close();
			return false;
		}
	}
	
	@Override
	public void stopAudio() {
		try {
			this.queue.clear();
			this.stopAudio = true;
		}
		catch (final Exception exc) {
			//
		}
	}
	
	@Override
	public void updateAudioOptions(final AudioStreamOptions options) {
		this.sfxMute = !options.isEnable();
		this.sfxVolume = this.sfxMute ? 0.0 : options.getVolume();
	}
	
	public void updateMusicAudioOptions(final AudioStreamOptions options) {
		this.musicMute = !options.isEnable();
		if (this.musicMute) {
			this.musicVolume = 0.0;
		}
		else {
			this.musicVolume = options.getVolume();
		}
	}
	
	private void applyOptions(final SourceDataLine line, final boolean force) {
		if (force) {
			final FloatControl control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
			control.setValue((float) Math.log10(1.0) * 20.0f);
		}
		if (force || this.curMute != (this.sfxMute && this.musicMute)) {
			this.curMute = (this.sfxMute && this.musicMute);
			
			final BooleanControl control = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);
			control.setValue(this.curMute);
		}
	}
	
	@Override
	public boolean isPlaying() {
		return this.playing;
	}
	
	@Override
	public BlockingQueue<AudioData> getQueue() {
		return this.queue;
	}
	
	public AudioData getMusicQueue() {
		return this.musicQueue;
	}
	
	public void setMusicQueue(final AudioData musicQueue) {
		this.musicQueue = musicQueue;
	}
	
	public boolean isMusicPlaying() {
		return this.musicPlaying;
	}
	
	public boolean isStopMusic() {
		return this.stopMusic;
	}
	
	public void setStopMusic(final boolean stopMusic) {
		this.stopMusic = stopMusic;
	}
	
	@Override
	public void exit() {
		this.exit = true;
	}
	
	private static class AudioPlayerStream implements AutoCloseable {
		
		private InputStream inStream;
		
		private AudioInputStream inAudio;
		
		private AudioInputStream convertedAudio;
		
		private Info info;
		
		@Override
		public void close() throws Exception {
			if (this.convertedAudio != null) {
				try {
					this.convertedAudio.close();
				}
				catch (final Exception exc) {
					LOGGER.error(exc.getMessage(), exc);
				}
				this.convertedAudio = null;
			}
			if (this.inAudio != null) {
				try {
					this.inAudio.close();
				}
				catch (final Exception exc) {
					LOGGER.error(exc.getMessage(), exc);
				}
				this.inAudio = null;
			}
			if (this.inStream != null) {
				try {
					this.inStream.close();
				}
				catch (final Exception exc) {
					LOGGER.error(exc.getMessage(), exc);
				}
				this.inStream = null;
			}
			this.info = null;
		}
	}
	
	private static class AudioBuffer {
		
		byte[] bytes = new byte[2048];
		
		int size;
		
	}
	
}
