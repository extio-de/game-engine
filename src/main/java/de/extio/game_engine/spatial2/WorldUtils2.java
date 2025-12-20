package de.extio.game_engine.spatial2;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import de.extio.game_engine.spatial2.model.Area2;
import de.extio.game_engine.spatial2.model.CoordD2;
import de.extio.game_engine.spatial2.model.CoordI2;
import de.extio.game_engine.spatial2.model.Edge2;
import de.extio.game_engine.spatial2.model.HasPosition2;
import de.extio.game_engine.spatial2.model.HasPositionAndDimension2;
import de.extio.game_engine.spatial2.model.ImmutableCoordD2;
import de.extio.game_engine.spatial2.model.ImmutableCoordI2;
import de.extio.game_engine.spatial2.model.MutableCoordD2;
import de.extio.game_engine.spatial2.model.MutableCoordI2;
import de.extio.game_engine.spatial2.model.RectI2;
import de.extio.game_engine.util.rng.ThreadLocalXorShift128Random;

/**
 * Library for geometric problems
 */
public final class WorldUtils2 {
	
	/**
	 * The Pythagorean theorem
	 * @param v Vector
	 * @return Length of vector v
	 */
	public static double getDistance(final CoordI2 v) {
		return Math.sqrt(Math.pow(v.getX(), 2.0) + Math.pow(v.getY(), 2.0));
	}
	
	/**
	 * The Pythagorean theorem
	 * @param v Vector
	 * @return Length of vector v
	 */
	public static double getDistance(final CoordD2 v) {
		return Math.sqrt(Math.pow(v.getX(), 2.0) + Math.pow(v.getY(), 2.0));
	}
	
	/**
	 * The Pythagorean theorem
	 * @param p0 Coordinate 0
	 * @param p1 Coordinate 1
	 * @return Distance
	 */
	public static double getDistance(final HasPosition2 p0, final HasPosition2 p1) {
		return getDistance(p0.getPosition(), p1.getPosition());
	}
	
	/**
	 * The Pythagorean theorem
	 * @param c0 Coordinate 0
	 * @param c1 Coordinate 1
	 * @return Distance
	 */
	public static double getDistance(final CoordI2 c0, final CoordI2 c1) {
		return Math.sqrt(Math.pow(c0.getX() - c1.getX(), 2.0) + Math.pow(c0.getY() - c1.getY(), 2.0));
	}
	
	/**
	 * The Pythagorean theorem
	 * @param c0 Coordinate 0
	 * @param c1 Coordinate 1
	 * @return Distance
	 */
	public static double getDistance(final CoordD2 c0, final CoordD2 c1) {
		return Math.sqrt(Math.pow(c0.getX() - c1.getX(), 2.0) + Math.pow(c0.getY() - c1.getY(), 2.0));
	}
	
	/**
	 * Calculate the angle between 2 vectors
	 * @param v0 Vector 0
	 * @param v1 Vector 1
	 * @return Angle (radians)
	 */
	public static double getVectorAngle(final CoordI2 v0, final CoordI2 v1) {
		return Math.atan2(v0.getX() * v1.getY() - v0.getY() * v1.getX(), v0.getX() * v1.getX() + v0.getY() * v1.getY());
	}
	
	/**
	 * Calculate the angle between 2 vectors
	 * @param v0 Vector 0
	 * @param v1 Vector 1
	 * @return Angle (radians)
	 */
	public static double getVectorAngle(final CoordD2 v0, final CoordD2 v1) {
		//		final double dotV = v0.getX() * v1.getX() + v0.getY() * v1.getY();
		//		final double distV = getDistance(v0) * getDistance(v1);
		//		if (distV == 0.0) {
		//			return 0.0;
		//		}
		//		return Math.acos(Math.min(dotV / distV, 1.0));
		return Math.atan2(v0.getX() * v1.getY() - v0.getY() * v1.getX(), v0.getX() * v1.getX() + v0.getY() * v1.getY());
	}
	
	/**
	 * Calculate the direction of a vector
	 * @param v0 Vector 0
	 * @param v1 Vector 1
	 * @return Angle (radians)
	 */
	public static double getVectorAngle(final CoordI2 v) {
		return Math.atan2(v.getX(), v.getY());
	}
	
	/**
	 * Calculate the direction of a vector
	 * @param v0 Vector 0
	 * @param v1 Vector 1
	 * @return Angle (radians)
	 */
	public static double getVectorAngle(final CoordD2 v) {
		return Math.atan2(v.getX(), v.getY());
	}
	
	/**
	 * Calculate the dot product
	 * @param v0 Vector 0
	 * @param v1 Vector 1
	 * @return Dot product
	 */
	public static double getVectorDotProduct(final CoordD2 v0, final CoordD2 v1) {
		return v0.getX() * v1.getX() + v0.getY() * v1.getY();
	}
	
	/**
	 * Rotates a vector
	 * @return Rotated vector
	 */
	public static CoordD2 rotateVector(final CoordD2 v, final double θ) {
		return ImmutableCoordD2.create(v.getX() * Math.cos(θ) - v.getY() * Math.sin(θ), v.getX() * Math.sin(θ) + v.getY() * Math.cos(θ));
	}
	
	/**
	 * Checks whether coord is within bounds (e.g. composite entity)
	 * @param hasPosition
	 * @return
	 */
	public static boolean isInBounds(final HasPosition2 hasPosition, final CoordI2 dimension) {
		return isInBounds(hasPosition.getPosition(), dimension);
	}
	
	/**
	 * Checks whether coord is within bounds (e.g. composite entity)
	 * @param coord
	 * @return
	 */
	public static boolean isInBounds(final CoordI2 coord, final CoordI2 dimension) {
		if (coord == null ||
				coord.getX() < 0 || coord.getX() >= dimension.getX() ||
				coord.getY() < 0 || coord.getY() >= dimension.getY()) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Signum function that returns -1 or 1.
	 * @param n
	 * @return -1 if n is negative and 1 if n is positive (including 0)
	 */
	public static int copySign(final int n) {
		return (Integer.signum(n) >> 31) | 1;
	}
	
	/**
	 * Signum function that returns -1 or 1.
	 * @param n
	 * @return -1 if n is negative and 1 if n is positive (including 0)
	 */
	public static int copySign(final double n) {
		return ((int) Math.signum(n) >> 31) | 1;
	}
	
	/**
	 * Intersects 2 recangles
	 * <!-- @deprecated Use Area2 based function intersectAreas() instead -->
	 * @param r0 Rectangle 0
	 * @param r1 Rectangle 1
	 * @return The intersected area if r0 and r1 overlap, else null
	 */
	//	@Deprecated
	public static RectI2 intersectRectangles(final RectI2 r0, final RectI2 r1) {
		final CoordI2 size0 = ImmutableCoordI2.create(r0.getC1().getX() - r0.getC0().getX(), r0.getC1().getY() - r0.getC0().getY());
		final CoordI2 size1 = ImmutableCoordI2.create(r1.getC1().getX() - r1.getC0().getX(), r1.getC1().getY() - r1.getC0().getY());
		final Rectangle rect0 = new Rectangle(r0.getC0().getX(), r0.getC0().getY(), size0.getX(), size0.getY());
		final Rectangle rect1 = new Rectangle(r1.getC0().getX(), r1.getC0().getY(), size1.getX(), size1.getY());
		
		final Rectangle intersection = rect0.intersection(rect1);
		if (intersection.isEmpty()) {
			return null;
		}
		
		return new RectI2(
				ImmutableCoordI2.create((int) intersection.getX(), (int) intersection.getY()),
				ImmutableCoordI2.create((int) (intersection.getX() + intersection.getWidth()), (int) (intersection.getY() + intersection.getHeight())));
	}
	
	/**
	 * Intersects 2 areas
	 * @param r0 Area 0
	 * @param r1 Area 1
	 * @return The intersected area if r0 and r1 overlap, else null
	 */
	public static Area2 intersectAreas(final HasPositionAndDimension2 a0, final HasPositionAndDimension2 a1) {
		final Rectangle rect0 = new Rectangle(a0.getPosition().getX(), a0.getPosition().getY(), a0.getDimension().getX(), a0.getDimension().getY());
		final Rectangle rect1 = new Rectangle(a1.getPosition().getX(), a1.getPosition().getY(), a1.getDimension().getX(), a1.getDimension().getY());
		
		final Rectangle intersection = rect0.intersection(rect1);
		if (intersection.isEmpty()) {
			return null;
		}
		
		return new Area2(
				ImmutableCoordI2.create((int) intersection.getX(), (int) intersection.getY()),
				ImmutableCoordI2.create((int) intersection.getWidth(), (int) intersection.getHeight()));
	}
	
	/**
	 * Checks whether 2 objects intersect
	 * @param obj0 HasPositionAndDimension2 object 0
	 * @param obj1 HasPositionAndDimension2 object 1
	 * @return true / false whether obj0 intersects with obj1
	 */
	public static boolean intersects(final HasPositionAndDimension2 obj0, final HasPositionAndDimension2 obj1) {
		return intersects(obj0.getPosition(), obj0.getDimension(), obj1.getPosition(), obj1.getDimension());
	}
	
	/**
	 * Checks whether 2 objects intersect
	 * @param position0 position of object 0
	 * @param dimension0 dimension of object 0
	 * @param position1 position of object 1
	 * @param dimension1 dimension of object 1
	 * @return true / false whether obj0 intersects with obj1
	 */
	public static boolean intersects(final CoordI2 position0, final CoordI2 dimension0, final CoordI2 position1, final CoordI2 dimension1) {
		int d0x = dimension0.getX();
		int d0y = dimension0.getY();
		int d1x = dimension1.getX();
		int d1y = dimension1.getY();
		if (d0x <= 0 || d0y <= 0 || d1x <= 0 || d1y <= 0) {
			return false;
		}
		
		d0x += position0.getX();
		d0y += position0.getY();
		d1x += position1.getX();
		d1y += position1.getY();
		
		return ((d0x < position0.getX() || d0x > position1.getX()) &&
				(d0y < position0.getY() || d0y > position1.getY()) &&
				(d1x < position1.getX() || d1x > position0.getX()) &&
				(d1y < position1.getY() || d1y > position0.getY()));
	}
	
	
	/**
	 * Builds an aligned Area2 from arbitrary points 
	 * @return
	 */
	public static Area2 pointsToArea(final CoordI2... points) {
		if (points == null || points.length == 0) {
			return null;
		}
		
		final CoordI2 corner0 = MutableCoordI2.create(points[0]);
		final CoordI2 corner1 = MutableCoordI2.create(points[0]).add(1, 1);
		for (int i = 1; i < points.length; i++) {
			corner0.setXY(Math.min(corner0.getX(), points[i].getX()), Math.min(corner0.getY(), points[i].getY()));
			corner1.setXY(Math.max(corner1.getX(), points[i].getX() + 1), Math.max(corner1.getY(), points[i].getY() + 1));
		}
		
		final CoordI2 dimension = ImmutableCoordI2.create(Math.max(1, corner1.getX() - corner0.getX()), Math.max(1, corner1.getY() - corner0.getY()));
		return new Area2(ImmutableCoordI2.create(corner0), dimension);
	}
	
	/**
	 * Calculates the point of intersection to intercept target from origin with velocity velOrigin
	 * @param pOrigin_ Position of origin (e.g. ship that intercepts target)
	 * @param velOrigin Velocity of origin
	 * @param pTarget_ Position of target
	 * @param vTarget_ Velocity vector of target
	 * @return Point of interception or null if interception is not possible 
	 */
	public static CoordI2 intercept(final CoordI2 pOrigin_, final double velOrigin, final CoordI2 pTarget_, final CoordD2 vTarget_) {
		final CoordD2 pOrigin = ImmutableCoordD2.create(pOrigin_);
		final CoordD2 pTarget = ImmutableCoordD2.create(pTarget_);
		final CoordD2 vTarget = ImmutableCoordD2.create(vTarget_);
		
		final double a = (vTarget.getX() * vTarget.getX()) + (vTarget.getY() * vTarget.getY()) - (velOrigin * velOrigin);
		final double b = 2 * ((pTarget.getX() * vTarget.getX()) + (pTarget.getY() * vTarget.getY()) - (pOrigin.getX() * vTarget.getX()) - (pOrigin.getY() * vTarget.getY()));
		final double c = (pTarget.getX() * pTarget.getX()) + (pTarget.getY() * pTarget.getY()) + (pOrigin.getX() * pOrigin.getX()) + (pOrigin.getY() * pOrigin.getY()) - (2 * pOrigin.getX() * pTarget.getX()) - (2 * pOrigin.getY() * pTarget.getY());
		final double t0 = (-b + Math.sqrt((b * b) - (4.0 * a * c))) / (2.0 * a);
		final double t1 = (-b - Math.sqrt((b * b) - (4.0 * a * c))) / (2.0 * a);
		
		final boolean t0Pass = !Double.isNaN(t0) && t0 > 0.0;
		final boolean t1Pass = !Double.isNaN(t1) && t1 > 0.0;
		double t;
		if (t0Pass && t1Pass) {
			t = Math.min(t0, t1);
		}
		else if (t0Pass) {
			t = t0;
		}
		else if (t1Pass) {
			t = t1;
		}
		else {
			return null;
		}
		
		return ImmutableCoordI2.create(MutableCoordD2.create(vTarget).multiply(t).add(pTarget));
	}
	
	/**
	 * Returns the sector the point is located from source's view  
	 * @param point
	 * @param source
	 * @return Sector (Edge2)
	 */
	public static Edge2 getSector(final CoordI2 point, final HasPositionAndDimension2 source) {
		if (ImmutableCoordI2.zero().equals(source.getDimension())) {
			throw new IllegalArgumentException("Source's dimension is zero");
		}
		
		final DistanceAndSector dists[] = new DistanceAndSector[4];
		final CoordI2 targetPos = ImmutableCoordI2.create(source.getPosition());
		
		dists[0] = new DistanceAndSector(getDistance(point, targetPos), 0b1);
		dists[1] = new DistanceAndSector(getDistance(point, targetPos.add(source.getDimension().getX(), 0)), 0b10);
		dists[2] = new DistanceAndSector(getDistance(point, targetPos.add(0, source.getDimension().getY())), 0b100);
		dists[3] = new DistanceAndSector(getDistance(point, targetPos.add(source.getDimension().getX(), source.getDimension().getY())), 0b1000);
		
		Arrays.sort(dists, (e0, e1) -> Double.compare(e0.dist, e1.dist));
		
		final int sector = dists[0].sector | dists[1].sector;
		
		if (sector == 0b11) {
			return Edge2.TOP;
		}
		else if (sector == 0b101) {
			return Edge2.LEFT;
		}
		else if (sector == 0b1010) {
			return Edge2.RIGHT;
		}
		else if (sector == 0b1100) {
			return Edge2.BOTTOM;
		}
		throw new RuntimeException("Unknown sector 0b" + Integer.toString(sector, 2));
	}
	
	/**
	 * Calculates the pedal of altitude c of triangle A.B,C
	 * @param a Point A
	 * @param b Point B
	 * @param c Point C
	 * @return Pedal of altitude c
	 */
	public static CoordD2 getPedal(final CoordI2 a, final CoordI2 b, final CoordI2 c) {
		final double distA = getDistance(a, b);
		if (distA == 0.0) {
			return null;
		}
		final double distASquare = Math.pow(distA, 2.0);
		final double distB = getDistance(b, c);
		final double distBSquare = Math.pow(distB, 2.0);
		final double distC = getDistance(c, a);
		final double distCSquare = Math.pow(distC, 2.0);
		
		final double altA = Math.sqrt((2.0 * ((distASquare * distBSquare) + (distBSquare * distCSquare) + (distCSquare * distASquare))) - (Math.pow(distA, 4.0) + Math.pow(distB, 4.0) + Math.pow(distC, 4.0))) / (2.0 * distA);
		if (Double.isNaN(altA)) {
			return null;
		}
		final double alpha = getVectorAngle(b.substract(a), c.substract(a));
		final double scaleA = (Math.sqrt(distCSquare - Math.pow(altA, 2.0)) / distA) * copySign(Math.PI / 2.0 - Math.abs(alpha));
		if (Double.isNaN(scaleA)) {
			return null;
		}
		
		return ImmutableCoordD2.create(
				(((double) b.getX() - (double) a.getX()) * scaleA) + a.getX(),
				(((double) b.getY() - (double) a.getY()) * scaleA) + a.getY());
	}
	
	/**
	 * Get's the pedal of altitude c of triangle A.B,C or the closest point on line segment a,b 
	 * <pre>
	 *   a-----p2-------b   p1
	 *         |         \  |
	 *         |          \ |
	 *         |           \|
	 *         c2            c1
	 * </pre>
	 * 
	 * Returns null if there is no pedal.
	 */
	public static CoordD2 getPedalOnLineSegmentAB(final CoordI2 a, final CoordI2 b, final CoordI2 c) {
		final CoordD2 pedal = getPedal(a, b, c);
		if (pedal == null) {
			return null;
		}
		
		double x = pedal.getX();
		final double minX = Math.min(a.getX(), b.getX());
		if (x < minX) {
			x = minX;
		}
		final double maxX = Math.max(a.getX(), b.getX());
		if (x > maxX) {
			x = maxX;
		}
		
		double y = pedal.getY();
		final double minY = Math.min(a.getY(), b.getY());
		if (y < minY) {
			y = minY;
		}
		final double maxY = Math.max(a.getY(), b.getY());
		if (y > maxY) {
			y = maxY;
		}
		
		return ImmutableCoordD2.create(x, y);
	}
	
	/**
	 * Get's the closest point at area target from origin
	 * <pre>
	 * 		*-----*
	 *      |     |
	 *      |     x _______ origin
	 *      |     |
	 *      *-----*
	 * </pre>
	 * @param origin
	 * @param target
	 * @return
	 */
	public static CoordI2 getClosestPoint(final CoordI2 origin, final HasPositionAndDimension2 target) {
		double minDistance = Double.MAX_VALUE;
		CoordI2 result = null;
		
		final List<CoordI2> vertices = getVertices(target);
		for (int i = 1; i < 4; i++) {
			final CoordD2 pedal = getPedalOnLineSegmentAB(vertices.get(i - 1), vertices.get(i), origin);
			if (pedal != null) {
				final CoordI2 pedalI = pedal.toImmutableCoordI2();
				final double distance = getDistance(origin, pedalI);
				if (distance < minDistance) {
					minDistance = distance;
					result = pedalI;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Calculates waypoints to evade a collision between origin and target.
	 * @param pOrigin Origin
	 * @param velOrigin Origin velocity
	 * @param vOrigin Origin velocity vector, origin course not necessarily the current velocity vector 
	 * @param pTarget Target
	 * @param vTarget Target velocity vector
	 * @param oTarget Collision offset on target (entityPos)
	 * @param pInterception (Optional) Point of interception. Can be null, in this case it is calculated based on the velocity of source and the velocity vector of target.
	 * @return Waypoints for evasion or null if there is no collision
	 */
	public static List<CoordI2> evade(final HasPositionAndDimension2 origin, final double velOrigin, final CoordD2 vOrigin, final HasPositionAndDimension2 target, final CoordD2 vTarget, final CoordI2 oTarget, final CoordI2 pInterception_) {
		final double THRESHOLD_NOT_MOVING = 0.2;
		
		final CoordI2 pOrigin = ImmutableCoordI2.create(origin.getPosition()).add(ImmutableCoordI2.create(origin.getDimension()).divide(2));
		final CoordI2 pTarget = ImmutableCoordI2.create(target.getPosition()).add(ImmutableCoordI2.create(target.getDimension()).divide(2));
		final double velTarget = getDistance(vTarget);
		
		if (velOrigin < THRESHOLD_NOT_MOVING) {
			// Corner case: Origin is not moving
			
			final CoordD2 vOriginToTarget = ImmutableCoordD2.create(pTarget).substract(ImmutableCoordD2.create(pOrigin));
			CoordD2 vEvasion = MutableCoordD2
					.create(-vOriginToTarget.getY(), vOriginToTarget.getX()) // Rotate by PI/2
					.toVNorm()
					.multiply(getDistance(origin.getDimension()) * 2.0);
			vEvasion = vEvasion.setXY(Math.ceil(vEvasion.getX()), Math.ceil(vEvasion.getY()));
			return List.of(pOrigin.add(ImmutableCoordI2.create(vEvasion)));
		}
		
		// Point of interception
		
		CoordI2 pInterception;
		if (pInterception_ == null) {
			pInterception = intercept(pOrigin, velOrigin, pTarget, vTarget);
			if (pInterception == null) {
				return null;
			}
		}
		else {
			pInterception = pInterception_;
		}
		
		// Create vertices
		
		final CoordI2 boxDim = ImmutableCoordI2.create(
				Math.max(origin.getDimension().getX(), origin.getDimension().getY()) + 4,
				Math.max(origin.getDimension().getX(), origin.getDimension().getY()) + 4);
		final CoordI2 targetDimHalf = target.getDimension().divide(2);
		final Area2 box = new Area2(pInterception
				.add(targetDimHalf.substract(oTarget))
				.substract(targetDimHalf)
				.substract(boxDim),
				targetDimHalf
						.add(boxDim)
						.multiply(2));
		final List<CoordI2> vertices = getVertices(box);
		
		// Rule out one or more vertices, because ...
		
		if (velTarget >= THRESHOLD_NOT_MOVING && Math.abs(getVectorAngle(vOrigin, vTarget)) < Math.PI / 4.0) { // Velocity vectors are similar -> collision from behind
			// Remove both vertices in origin move direction 
			
			int removeVertexIdxLow = -1;
			int removeVertexIdxHigh = -1;
			
			double aLow = Math.PI;
			double aHigh = 0.0;
			for (int i = 0; i < vertices.size(); i++) {
				final CoordI2 curVertex = vertices.get(i);
				
				final CoordD2 direction = pInterception.substract(curVertex).toImmutableCoordD2();
				final double a = Math.abs(getVectorAngle(vOrigin, direction));
				if (removeVertexIdxLow == -1 || a < aLow) {
					aLow = a;
					removeVertexIdxLow = i;
				}
				if (removeVertexIdxHigh == -1 || a > aHigh) {
					aHigh = a;
					removeVertexIdxHigh = i;
				}
			}
			
			vertices.set(removeVertexIdxLow, null);
			vertices.set(removeVertexIdxHigh, null);
		}
		else if (velTarget < velOrigin - 0.75) { // Origin speed is higher
			// Remove vertex farthest from origin move direction, but closest vertex will never be ruled out (-> primary)
			
			int safeVertexIdx = 0;
			double vLen = -1.0;
			for (int i = 0; i < vertices.size(); i++) {
				final CoordI2 curVertex = vertices.get(i);
				final double curVLen = getDistance(pOrigin, curVertex);
				if (vLen < 0.0 || curVLen < vLen) {
					vLen = curVLen;
					safeVertexIdx = i;
				}
			}
			
			int removeVertexIdx = 0;
			double aHigh = 0.0;
			for (int i = 0; i < vertices.size(); i++) {
				if (i == safeVertexIdx) {
					continue;
				}
				
				final CoordI2 curVertex = vertices.get(i);
				final double a = Math.abs(getVectorAngle(curVertex.substract(pOrigin).toImmutableCoordD2(), vOrigin));
				if (a > aHigh) {
					aHigh = a;
					removeVertexIdx = i;
				}
			}
			
			vertices.set(removeVertexIdx, null);
		}
		else {
			// Remove vertex closest to target move direction
			
			int removeVertexIdx = -1;
			
			double vLen = -1.0;
			for (int i = 0; i < vertices.size(); i++) {
				final CoordI2 curVertex = vertices.get(i);
				final CoordD2 curV = ImmutableCoordD2.create(pInterception.substract(curVertex)).add(vTarget);
				final double curVLen = getDistance(curV);
				if (vLen < 0.0 || curVLen < vLen) {
					vLen = curVLen;
					removeVertexIdx = i;
				}
			}
			
			vertices.set(removeVertexIdx, null);
		}
		
		// Get closest vertex -> primary
		
		int primaryVertexIdx = -1;
		double closestDist = -1.0;
		for (int i = 0; i < vertices.size(); i++) {
			if (vertices.get(i) != null) {
				final double curDist = getDistance(vertices.get(i), pOrigin);
				if (closestDist < 0.0 || curDist < closestDist) {
					closestDist = curDist;
					primaryVertexIdx = i;
				}
			}
		}
		
		// Choose secondary vertex
		
		final int candidate0 = (primaryVertexIdx + 1) % vertices.size();
		final int candidate1 = Math.floorMod(primaryVertexIdx - 1, vertices.size());
		int secondVertexIdx;
		if (vertices.get(candidate0) != null && vertices.get(candidate1) != null) {
			// Center vertex, choose secondary which is closer to origin velocity vector 
			final double a0 = getVectorAngle(pInterception.substract(pOrigin), vertices.get(candidate0).substract(pOrigin));
			final double a1 = getVectorAngle(pInterception.substract(pOrigin), vertices.get(candidate1).substract(pOrigin));
			if (Math.abs(a0) < Math.abs(a1)) {
				secondVertexIdx = candidate0;
			}
			else {
				secondVertexIdx = candidate1;
			}
		}
		// Outer vertex, there is only one choice for the secondary one
		else if (vertices.get(candidate0) != null) {
			secondVertexIdx = candidate0;
		}
		else {
			secondVertexIdx = candidate1;
		}
		
		// Check whether path from primary vertex to secondary intersects with origin path -> in this case skip the primary vertex except the closest vertex is in target velocity direction 
		
		//		final double a0 = getVectorAngle(pInterception.substract(pOrigin), vertices.get(primaryVertexIdx).substract(pOrigin));
		//		final double a1 = getVectorAngle(pInterception.substract(pOrigin), vertices.get(secondVertexIdx).substract(pOrigin));
		//		final double intersecting = Math.signum(a0 * a1);
		//		if (intersecting < 0.0) {
		//			if (closestDist < getDistance(removedVertex, pOrigin)) {
		//				primaryVertexIdx = secondVertexIdx;
		//				secondVertexIdx = -1;
		//			}
		//		}
		
		if (secondVertexIdx == -1 || vertices.get(secondVertexIdx) == null) {
			return List.of(vertices.get(primaryVertexIdx));
		}
		return List.of(vertices.get(primaryVertexIdx), vertices.get(secondVertexIdx));
	}
	
	/**
	 * <pre>  
	 *   x-----x
	 *   |     |
	 *   |     |
	 *   x-----x
	 * </pre>
	 * Returns the 4 vertices of a rectangle
	 * @param obj Rectangle
	 * @return 4 vertices ordered clock-wise
	 */
	public static List<CoordI2> getVertices(final HasPositionAndDimension2 obj) {
		final List<CoordI2> result = new ArrayList<>(4);
		final CoordI2 objPos = ImmutableCoordI2.create(obj.getPosition());
		result.add(objPos);
		result.add(objPos.add(obj.getDimension().getX(), 0));
		result.add(objPos.add(obj.getDimension()));
		result.add(objPos.add(0, obj.getDimension().getY()));
		return result;
	}
	
	/**
	 * <pre>  
	 *   x-----x
	 *   |     |
	 *   |     |
	 *   x-----x
	 * </pre>
	 * Returns the 4 sides of a rectangle
	 * @param obj Rectangle
	 * @return 4 sides ordered clock-wise
	 */
	public static List<Area2> getSides(final HasPositionAndDimension2 obj) {
		final List<Area2> result = new ArrayList<>(4);
		final CoordI2 objPos = ImmutableCoordI2.create(obj.getPosition());
		result.add(new Area2(objPos, ImmutableCoordI2.create(obj.getDimension().getX(), 1)));
		result.add(new Area2(objPos.add(obj.getDimension().getX(), 0), ImmutableCoordI2.create(1, obj.getDimension().getY())));
		result.add(new Area2(objPos.add(1, obj.getDimension().getY()), ImmutableCoordI2.create(obj.getDimension().getX(), 1)));
		result.add(new Area2(objPos.add(0, 1), ImmutableCoordI2.create(1, obj.getDimension().getY())));
		return result;
	}
	
	/**
	 * Returns a random point in a circle
	 * @param center of the circle
	 * @param radius of the circle
	 * @return random point in the circle
	 */
	public static CoordI2 getRandomPointInCircle(final CoordI2 center, final int radius) {
		return getRandomPointInCircle(center, radius, ThreadLocalXorShift128Random.current());
	}
	
	/**
	 * Returns a point in a circle
	 * @param center of the circle
	 * @param radius of the circle
	 * @param random rng
	 * @return random point in the circle
	 */
	public static CoordI2 getRandomPointInCircle(final CoordI2 center, final int radius, final Random random) {
		return MutableCoordD2
				.create(Math.cos(random.nextDouble() * Math.PI * 2.0 - Math.PI), Math.sin(random.nextDouble() * Math.PI * 2.0 - Math.PI))
				.multiply(radius)
				.toImmutableCoordI2()
				.add(center);
	}
	
	/**
	 * Returns a random point on a circle
	 * @param center of the circle
	 * @param radius of the circle
	 * @return random point in the circle
	 */
	public static CoordI2 getRandomPointOnCircle(final CoordI2 center, final int radius) {
		return getRandomPointOnCircle(center, radius, ThreadLocalXorShift128Random.current());
	}
	
	/**
	 * Returns a random point on a circle
	 * @param center of the circle
	 * @param radius of the circle
	 * @param random rng
	 * @return random point in the circle
	 */
	public static CoordI2 getRandomPointOnCircle(final CoordI2 center, final int radius, final Random random) {
		final double angle = random.nextDouble() * Math.PI * 2.0 - Math.PI;
		return MutableCoordD2
				.create(Math.cos(angle), Math.sin(angle))
				.multiply(radius)
				.toImmutableCoordI2()
				.add(center);
	}
	
	/**
	 * Returns a reproducible point on a circle
	 * @param center of the circle
	 * @param radius of the circle
	 * @param counter decides where the point is
	 * @return random point in the circle
	 */
	public static CoordI2 getReproduciblePointOnCircle(final CoordI2 center, final int radius, final AtomicInteger counter) {
		final double angle = counter.incrementAndGet() / 3.0 % Math.PI * 2.0 - Math.PI;
		return MutableCoordD2
				.create(Math.cos(angle), Math.sin(angle))
				.multiply(radius)
				.toImmutableCoordI2()
				.add(center);
	}
	
	/**
	 * Calculates edge required at c1 to block the path from c0 to c1. Swap both parameters to get edge required at c0 to block the path from c1 to c0.<br/>
	 * <br/>
	 *   c0==&gt; |c1<br/>
	 *   c0| &lt;==c1<br/>
	 * 
	 * @param c0 Position
	 * @param c1 Target
	 * @return Edges that are required as bit mask
	 */
	public static int calcEdgeRequirement(final CoordI2 c0, final CoordI2 c1) {
		final int dx = c0.getX() - c1.getX();
		final int dy = c0.getY() - c1.getY();
		
		int edgeReq = 0;
		if (dx < 0 && dy < 0) {
			edgeReq = Edge2.LEFT.getBit() | Edge2.TOP.getBit();
		}
		else if (dx < 0 && dy == 0) {
			edgeReq = Edge2.LEFT.getBit();
		}
		else if (dx < 0 && dy > 0) {
			edgeReq = Edge2.LEFT.getBit() | Edge2.BOTTOM.getBit();
		}
		else if (dx == 0 && dy < 0) {
			edgeReq = Edge2.TOP.getBit();
		}
		else if (dx == 0 && dy > 0) {
			edgeReq = Edge2.BOTTOM.getBit();
		}
		else if (dx > 0 && dy < 0) {
			edgeReq = Edge2.RIGHT.getBit() | Edge2.TOP.getBit();
		}
		else if (dx > 0 && dy == 0) {
			edgeReq = Edge2.RIGHT.getBit();
		}
		else if (dx > 0 && dy > 0) {
			edgeReq = Edge2.RIGHT.getBit() | Edge2.BOTTOM.getBit();
		}
		
		return edgeReq;
	}
	
	private static class DistanceAndSector {
		
		double dist;
		
		int sector;
		
		public DistanceAndSector(final double dist, final int sector) {
			this.dist = dist;
			this.sector = sector;
		}
		
	}
}
