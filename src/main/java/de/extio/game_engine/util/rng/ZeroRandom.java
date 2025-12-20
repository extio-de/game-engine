package de.extio.game_engine.util.rng;

import java.util.Random;

/**
 * Random implementation always returning 0
 */
@SuppressWarnings("serial")
public final class ZeroRandom extends Random {
	
	@Override
	protected int next(final int bits) {
		return 0;
	}
	
}
