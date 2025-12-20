package de.extio.game_engine.spatial2.model;

/**
 * Identifies a vertex or a side
 */
public enum Edge2 {
	
	RIGHT(),
	BOTTOM(),
	LEFT(),
	TOP();
	
	public final static Edge2[] VALUES_CACHED = new Edge2[] {
			RIGHT,
			BOTTOM,
			LEFT,
			TOP
	};
	
	private final static CoordD2 VD_BOTTOM = MutableCoordD2.create(0.0, 1.0);
	
	private final static CoordD2 VD_LEFT = MutableCoordD2.create(-1.0, 0.0);
	
	private final static CoordD2 VD_RIGHT = MutableCoordD2.create(1.0, 0.0);
	
	private final static CoordD2 VD_TOP = MutableCoordD2.create(0.0, -1.0);
	
	private final static CoordI2 VI_BOTTOM = ImmutableCoordI2.create(0, 1);
	
	private final static CoordI2 VI_LEFT = ImmutableCoordI2.create(-1, 0);
	
	private final static CoordI2 VI_RIGHT = ImmutableCoordI2.create(1, 0);
	
	private final static CoordI2 VI_TOP = ImmutableCoordI2.create(0, -1);
	
	public static Edge2 fromBit(final int bit) {
		if ((bit & RIGHT.bit) == RIGHT.bit) {
			return RIGHT;
		}
		else if ((bit & BOTTOM.bit) == BOTTOM.bit) {
			return BOTTOM;
		}
		else if ((bit & LEFT.bit) == LEFT.bit) {
			return LEFT;
		}
		else if ((bit & TOP.bit) == TOP.bit) {
			return TOP;
		}
		
		return null;
	}
	
	/**
	 * Rotates edge from RIGHT to destination clock-wise. BOTTOM rotates by 1 stop, LEFT by 2 and TOP by 3.
	 * @param dest stops
	 * @return Rotated edge2
	 */
	public Edge2 rotate(final Edge2 dest) {
		return Edge2.fromBit(Edge2.rotate(this.getBit(), dest));
	}
	
	/**
	 * Rotates edges represented by bit mask (Edge2::getBit)
	 * @param edges Bit mask of edges
	 * @param dest Rotate from RIGHT to destination clock-wise. BOTTOM rotates by 1 stop, LEFT by 2 and TOP by 3.
	 * @return Rotated bit mask
	 */
	public static int rotate(final int edges, final Edge2 dest) {
		// Formula:
		// bits: Number of significant bits, in our case 4 (4 edges)
		// shift: rotate by n steps
		//
		// ((x << shift) | (x >> (bits - shift))) & ((1 << bits) - 1)
		// |-----     Circular left shift   ----| |-- Cut significant bits --|
		
		if (dest == null) {
			return edges;
		}
		
		return ((edges << Integer.numberOfTrailingZeros(dest.bit)) | (edges >> (Edge2.VALUES_CACHED.length - Integer.numberOfTrailingZeros(dest.bit)))) & ((1 << Edge2.VALUES_CACHED.length) - 1);
	}
	
	/**
	 * Returns a vector with length 1 pointing to the direction edge2 representates
	 * @param edge2
	 * @return Vector
	 */
	public static CoordD2 toVectorD(final Edge2 edge2) {
		switch (edge2) {
			case BOTTOM:
				return VD_BOTTOM;
			case LEFT:
				return VD_LEFT;
			case RIGHT:
				return VD_RIGHT;
			case TOP:
				return VD_TOP;
		}
		
		return null;
	}
	
	/**
	 * Returns a vector with length 1 pointing to the direction edge2 representates
	 * @param edge2
	 * @return Vector
	 */
	public static CoordI2 toVectorI(final Edge2 edge2) {
		switch (edge2) {
			case BOTTOM:
				return VI_BOTTOM;
			case LEFT:
				return VI_LEFT;
			case RIGHT:
				return VI_RIGHT;
			case TOP:
				return VI_TOP;
		}
		
		return null;
	}
	
	public static String toString(final int edges) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Edges [");
		
		for (final Edge2 edge : Edge2.VALUES_CACHED) {
			if ((edges & (1 << edge.ordinal())) != 0) {
				sb.append(edge.name());
				sb.append(" ");
			}
		}
		
		sb.append("]");
		return sb.toString();
	}
	
	private int bit;
	
	Edge2() {
		this.bit = 1 << this.ordinal();
	}
	
	public int getBit() {
		return this.bit;
	}
	
}
