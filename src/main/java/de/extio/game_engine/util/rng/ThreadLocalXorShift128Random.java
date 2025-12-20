package de.extio.game_engine.util.rng;

import java.util.Random;

/**
 * Thread local version of XorShift128Random. This is the preferred way of using this generator.
 */
public final class ThreadLocalXorShift128Random {
	
	private final static ThreadLocal<Random> INSTANCE = ThreadLocal.withInitial(XorShift128Random::new);
	
	public static Random current() {
		return INSTANCE.get();
	}
	
	public static int nextInt(final int startInclusive, final int endExclusive) {
		if (startInclusive == endExclusive) {
			return startInclusive;
		}
		
		return startInclusive + current().nextInt(endExclusive - startInclusive);
	}
}
