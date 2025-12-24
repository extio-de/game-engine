package de.extio.game_engine.resource;

import java.util.List;

public record StaticResource(List<String> pathSegments, String resourceName) {

	@Override
	public final String toString() {
		if (this.pathSegments == null || this.pathSegments.isEmpty()) {
			return this.resourceName;
		} else {
			return String.join("/", this.pathSegments) + "/" + this.resourceName;
		}
	}

}
