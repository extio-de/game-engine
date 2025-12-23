package de.extio.game_engine.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Language {
	
	private String name;
	
	private String shortName;
	
	public Language() {
		
	}
	
	@JsonCreator
	public Language(@JsonProperty("name") final String name, @JsonProperty("shortName") final String shortName) {
		this.name = name;
		this.shortName = shortName;
	}
	
	public Language(final Language other) {
		this.name = other.name;
		this.shortName = other.shortName;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(final String name) {
		this.name = name;
	}
	
	public String getShortName() {
		return this.shortName;
	}
	
	public void setShortName(final String shortName) {
		this.shortName = shortName;
	}
	
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Language [name=");
		builder.append(this.name);
		builder.append(", shortName=");
		builder.append(this.shortName);
		builder.append("]");
		return builder.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.shortName == null) ? 0 : this.shortName.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (this.getClass() != obj.getClass())) {
			return false;
		}
		final Language other = (Language) obj;
		if (this.shortName == null) {
			if (other.shortName != null) {
				return false;
			}
		}
		else if (!this.shortName.equals(other.shortName)) {
			return false;
		}
		return true;
	}
	
}
