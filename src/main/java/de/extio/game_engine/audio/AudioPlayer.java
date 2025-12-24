package de.extio.game_engine.audio;

import java.util.concurrent.BlockingQueue;

import de.extio.game_engine.audio.AudioOptions.AudioStreamOptions;

interface AudioPlayer extends Runnable {
	
	void exit();
	
	void stopAudio();
	
	void updateAudioOptions(AudioStreamOptions options);
	
	boolean isPlaying();
	
	BlockingQueue<AudioData> getQueue();
	
}
