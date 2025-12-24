package de.extio.game_engine.audio;

import java.util.List;

import de.extio.game_engine.resource.StaticResource;

/**
 * Controls the audio system
 */
public interface AudioControl {
	
	/**
	 * Plays a collection of music tracks, music playback will be shuffled and repeat
	 * @param opener Optional opener track to be played first, will not repeat, can be null
	 * @param audioResources Collection of music tracks to be played
	 * @param standard Defines the given collection of music tracks as standard collection, see playStandardMusic()
	 */
	void playMusic(StaticResource opener, List<StaticResource> audioResources, boolean standard);
	
	/**
	 * Plays the standard collection of music tracks, music playback will be shuffled and repeat
	 */
	void playStandardMusic();
	
	/**
	 * Stops playing music
	 */
	void stopMusic();
	
	/**
	 * Plays a sound effect once
	 */
	void play(StaticResource audioResource);
	
	/**
	 * @return Copy of audio options to configure the audio system
	 */
	AudioOptions getAudioOptions();

	/**
	 * Applies the given audio options to the audio system
	 */
	void applyAudioOptions(AudioOptions audioOptions);

}
