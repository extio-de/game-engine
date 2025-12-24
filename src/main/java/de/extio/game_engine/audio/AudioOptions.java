package de.extio.game_engine.audio;

public class AudioOptions {
	
	private AudioStreamOptions sfxOptions;
	
	private AudioStreamOptions musicOptions;
	
	public AudioOptions() {
		this.sfxOptions = new AudioStreamOptions(true, 1.0, true);
		this.musicOptions = new AudioStreamOptions(true, 0.8, false);
	}
	
	public AudioOptions(final AudioOptions other) {
		this.sfxOptions = new AudioStreamOptions(other.sfxOptions.enable, other.sfxOptions.volume, other.sfxOptions.softwareMixing);
		this.musicOptions = new AudioStreamOptions(other.musicOptions.enable, other.musicOptions.volume, other.musicOptions.softwareMixing);
	}
	
	public void apply(final AudioOptions other) {
		this.sfxOptions = new AudioStreamOptions(other.sfxOptions.enable, other.sfxOptions.volume, other.sfxOptions.softwareMixing);
		this.musicOptions = new AudioStreamOptions(other.musicOptions.enable, other.musicOptions.volume, other.musicOptions.softwareMixing);
	}
	
	public AudioStreamOptions getSfxOptions() {
		return sfxOptions;
	}
	
	public void setSfxOptions(final AudioStreamOptions sfxOptions) {
		this.sfxOptions = sfxOptions;
	}
	
	public AudioStreamOptions getMusicOptions() {
		return musicOptions;
	}
	
	public void setMusicOptions(final AudioStreamOptions musicOptions) {
		this.musicOptions = musicOptions;
	}
	
	public class AudioStreamOptions {
		
		boolean enable;
		
		double volume;
		
		private boolean softwareMixing;
		
		public AudioStreamOptions() {
			
		}
		
		public AudioStreamOptions(final boolean enable, final double volume, final boolean softwareMixing) {
			this.enable = enable;
			this.volume = volume;
			this.softwareMixing = softwareMixing;
		}
		
		public boolean isEnable() {
			return this.enable;
		}
		
		public void setEnable(final boolean enable) {
			this.enable = enable;
		}
		
		public double getVolume() {
			return this.volume;
		}
		
		public void setVolume(final double volume) {
			this.volume = volume;
		}
		
		public boolean isSoftwareMixing() {
			return this.softwareMixing;
		}
		
		public void setSoftwareMixing(final boolean softwareMixing) {
			this.softwareMixing = softwareMixing;
		}
	}
	
}
