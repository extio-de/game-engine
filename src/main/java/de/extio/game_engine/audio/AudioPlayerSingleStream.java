package de.extio.game_engine.audio;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine.Info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import de.extio.game_engine.audio.AudioOptions.AudioStreamOptions;

final class AudioPlayerSingleStream implements AudioPlayer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AudioPlayerSingleStream.class);
	
	private final BlockingQueue<AudioData> queue = new LinkedBlockingQueue<>(5);
	
	private volatile boolean stopAudio;
	
	private volatile boolean playing;
	
	private volatile double volume;
	
	private volatile boolean mute;
	
	private volatile boolean exit;
	
	private double curVolume;
	
	private boolean curMute;
	
	private int notStartedAudioCnt;
	
	public AudioPlayerSingleStream() {
	}
	
	@Override
	public void run() {
		LOGGER.debug("start");
		
		while (!Thread.currentThread().isInterrupted() && !this.exit) {
			try {
				final AudioData data = this.queue.peek();
				if (data == null) {
					this.playing = false;
					Thread.sleep(5);
					continue;
				}
				
				this.playing = true;
				this.queue.remove();
				
				this.stopAudio = false;
				try (final InputStream stream = data.inputStream(); final AudioInputStream in = AudioSystem.getAudioInputStream(stream)) {
					final AudioFormat inFormat = in.getFormat();
					final int ch = inFormat.getChannels();
					final float rate = inFormat.getSampleRate();
					final AudioFormat outFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
					final Info info = new Info(SourceDataLine.class, outFormat);
					LOGGER.debug("Start {}: in {}; out {}", data.name(), inFormat, outFormat);
					
					try (final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
							final AudioInputStream inConverted = AudioSystem.getAudioInputStream(outFormat, in)) {
						
						if (line != null) {
							line.open(outFormat);
							line.start();
							
							boolean fading = false;
							double fadingVol = 0.0;
							this.notStartedAudioCnt = 0;
							this.applyOptions(line, true);
							
							final byte[] buffer = new byte[4096];
							for (int n = 0; n != -1; n = inConverted.read(buffer, 0, buffer.length)) {
								line.write(buffer, 0, n);
								
								if (Thread.currentThread().isInterrupted() || this.exit) {
									break;
								}
								else if (this.stopAudio) {
									if (!fading) {
										fading = true;
										fadingVol = this.volume;
									}
									
									fadingVol -= 0.01;
									if (fadingVol <= 0.0) {
										break;
									}
									
									final FloatControl control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
									control.setValue((float) Math.log10(fadingVol) * 20.0f);
								}
								else {
									this.applyOptions(line, false);
								}
							}
							
							line.drain();
							line.stop();
						}
					}
					finally {
						LOGGER.debug("Stop {}", data.name());
					}
				}
			}
			catch (final Exception exc) {
				if (exc instanceof InterruptedException) {
					LOGGER.info(exc.getClass().getName());
					break;
				}
				LOGGER.error(exc.getMessage(), exc);
				
				if (++this.notStartedAudioCnt > 5) {
					this.notStartedAudioCnt = 0;
					this.exit = true;
					LOGGER.error("Too many audio playback errors, shutting down audio player.");
				}
			}
		}
		
		this.playing = false;
		LOGGER.debug("end");
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
		this.volume = options.getVolume();
		this.mute = !options.isEnable();
	}
	
	private void applyOptions(final SourceDataLine line, final boolean force) {
		if (force || this.volume != this.curVolume) {
			this.curVolume = this.volume;
			
			if (this.curVolume >= 0.0 && this.curVolume <= 1.0) {
				final FloatControl control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
				control.setValue((float) Math.log10(this.curVolume) * 20.0f);
			}
		}
		if (force || this.curMute != this.mute) {
			this.curMute = this.mute;
			
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
	
	@Override
	public void exit() {
		this.exit = true;
	}
	
}
