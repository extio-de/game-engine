package de.extio.game_engine.util.rng;

import java.util.UUID;

/**
 * Optimized factory to create a random UUID
 */
public final class FastRandomUUID {
	
	/**
	 * Creates a random UUID that has been created fast without draining the operating system's pool of entropy but is not safe for security relevant use (but this is usually not our use case anyway)
	 */
	public static UUID create() {
		final var cur = ThreadLocalXorShift128Random.current();
		return new UUID(cur.nextLong(), cur.nextLong());
	}
	
}
