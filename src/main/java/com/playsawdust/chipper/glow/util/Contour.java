package com.playsawdust.chipper.glow.util;

import java.util.ArrayList;
import java.util.Iterator;

import org.joml.Matrix3dc;
import org.joml.Vector2d;
import org.joml.Vector3d;

import com.google.common.base.Preconditions;


/**
 * Represents a non-convex shape consisting of lines and quadratic bezier curves.
 */
public class Contour implements Iterable<Contour.ShapeBoundary<?>> {
	private ArrayList<ShapeBoundary<?>> boundary = new ArrayList<>();
	
	public void getApproximation(ArrayList<LineSegment> result, double curveAccuracyHint) {
		for(ShapeBoundary<?> segment : boundary) {
			if (segment instanceof LineSegment) {
				result.add((LineSegment) segment.copy());
			} else if (segment instanceof BezierLineSegment) {
				((BezierLineSegment) segment).decompose(result, curveAccuracyHint);
			}
		}
	}
	
	/** NOT FOR GENERAL USE! This just stuffs an arbitrary boundary part into the contour with no regard to whether it matches up with previous lines.
	 * So don't use unless yo have a really good idea of what makes a valid contour!
	 */
	public void addBoundarySegment(ShapeBoundary<?> segment) {
		boundary.add(segment);
	}
	
	@Override
	public Iterator<ShapeBoundary<?>> iterator() {
		return boundary.iterator();
	}
	
	public Contour copy() {
		Contour result = new Contour();
		
		for(ShapeBoundary<?> segment : boundary) {
			result.addBoundarySegment(segment.copy());
		}
		
		return result;
	}
	
	/** Transforms THIS contour in-place! */
	public void transform(Matrix3dc matrix) {
		Vector3d vec = new Vector3d();
		for(ShapeBoundary<?> segment : boundary) {
			matrix.transform(segment.x1, segment.y1, 0, vec);
			segment.x1 = vec.x;
			segment.y1 = vec.y;
			matrix.transform(segment.x2, segment.y2, 0, vec);
			segment.x2 = vec.x;
			segment.y2 = vec.y;
			if (segment instanceof BezierLineSegment) {
				BezierLineSegment bezier = (BezierLineSegment) segment;
				matrix.transform(bezier.xc, bezier.yc, 0, vec);
				bezier.xc = vec.x;
				bezier.yc = vec.y;
			}
		}
	}
	
	public VectorShape toShape() {
		return new VectorShape(this);
	}
	
	public static Builder from(double x, double y) {
		return new Builder(x, y);
	}
	
	public static abstract class ShapeBoundary<T extends ShapeBoundary<T>> {
		double x1;
		double y1;
		double x2;
		double y2;
		
		public double getX1() { return x1; }
		public double getY1() { return y1; }
		public double getX2() { return x2; }
		public double getY2() { return y2; }
		
		public abstract double length();
		public abstract ShapeBoundary<T> copy();
		public abstract void subdivide(T a, T b);
	}
	
	public static class LineSegment extends ShapeBoundary<LineSegment> {
		
		public LineSegment(double x1, double y1, double x2, double y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		
		@Override
		public void subdivide(LineSegment a, LineSegment b) {
			double ax = x1;
			double ay = y1;
			double midX = (x1+x2)/2;
			double midY = (y1+y2)/2;
			double bx = x2;
			double by = y2;
			
			a.x1 = ax;
			a.y1 = ay;
			a.x2 = midX;
			a.y2 = midY;
			
			b.x1 = midX;
			b.y1 = midY;
			b.x2 = bx;
			b.y2 = by;
		}
		
		@Override
		public double length() {
			double dx = Math.abs(x2-x1);
			double dy = Math.abs(y2-y1);
			return Math.sqrt(dx*dx + dy*dy);
		}
		
		@Override
		public LineSegment copy() {
			return new LineSegment(x1, y1, x2, y2);
		}
	}
	
	private static double quadratic(double a, double b, double c, double t) {
		double aTerm = c * Math.pow(t, 2);
		double bTerm = b * 2 * t * (1 - t);
		double cTerm = a * Math.pow((1 - t), 2);
		
		return aTerm + bTerm + cTerm;
	}
	
	public static class BezierLineSegment extends ShapeBoundary<BezierLineSegment> {
		double xc;
		double yc;
		
		public BezierLineSegment(double x1, double y1, double xc, double yc, double x2, double y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			
			this.xc = xc;
			this.yc = yc;
		}
		
		public double getControlX() { return xc; }
		public double getControlY() { return yc; }
		
		/**
		 * Subdivide this segment into two equivalent bezier segments, then places the results into a and b.
		 * 
		 * <p>Note: It is safe to call this method with this object itself as one of the arguments.
		 */
		@Override
		public void subdivide(BezierLineSegment a, BezierLineSegment b) {
			double ax = this.x1;
			double ay = this.y1;
			
			double bx = this.x2;
			double by = this.y2;
			
			double acx = (this.x1+xc)/2.0;
			double acy = (this.x1+yc)/2.0;
			double bcx = (this.x2+xc)/2.0;
			double bcy = (this.y2+yc)/2.0;
			
			double midX = quadratic(x1, xc, x2, 0.5);
			double midY = quadratic(y1, yc, y2, 0.5);
			
			a.x1 = ax;
			a.y1 = ay;
			a.x2 = midX;
			a.y2 = midY;
			a.xc = acx;
			a.yc = acy;
			
			b.x1 = midX;
			b.y1 = midY;
			b.x2 = bx;
			b.y2 = by;
			b.xc = bcx;
			b.yc = bcy;
		}
		
		/**
		 * Decomposes this curve into an acceptable number of LineSegments, attempting to make them no longer than curveAccuracyHint.
		 * Curve length is an approximation, so some segments may be longer or shorter than the hint.
		 * LineSegments are added to the end of the list, so this can be used to decompose multiple curves into the same list.
		 */
		public void decompose(ArrayList<LineSegment> lineSegments, double curveAccuracyHint) {
			Preconditions.checkArgument(curveAccuracyHint>0, "Curve accuracy cannot be zero or negative");
			int segments = (int) (this.length() / curveAccuracyHint);
			
			double stepSize = 1.0 / segments;
			double t = 0;
			double lastX = x1;
			double lastY = y1;
			while(t<1) {
				double x = quadratic(x1, xc, x2, t);
				double y = quadratic(y1, yc, y2, t);
				lineSegments.add(new LineSegment(lastX, lastY, x, y));
				
				t+= stepSize;
				lastX = x;
				lastY = y;
			}
			lineSegments.add(new LineSegment(lastX, lastY, x2, y2));
		}
		
		/**
		 * This is a *fast approximation* of the length along the bezier. The actual length can only be found by
		 * integrating across the arc, and that's too slow for us.
		 */
		@Override
		public double length() {
			//DA and DB are the pessimistic distances along the tangent lines
			double dax = Math.abs(x1-xc);
			double day = Math.abs(y1-yc);
			double da = Math.sqrt(dax*dax + day*day);
			
			double dbx = Math.abs(x2-xc);
			double dby = Math.abs(y2-yc);
			double db = Math.sqrt(dbx*dbx + dby*dby);
			
			double pessimistic = da+db;
			
			//DAB is the optimistic straight-line distance from one endpoint to the other
			double dabx = Math.abs(x2-x1);
			double daby = Math.abs(y2-y1);
			double dab = Math.sqrt(dabx*dabx + daby*daby);
			
			// 2/3 of the straight-line length plus 1/3 of the pessimistic length seems to give a good guestimate
			return (dab * 2.0 / 3.0) + (pessimistic / 3.0);
		}
		
		/**
		 * Gets the local curvature of this quadratic curve. Returns -1 if the curve is a straight line, 0 if
		 * there is a 90-degree curve, and 1 if the curve is so intense that it is degenerate (has no area).
		 */
		public double curvature() {
			Vector2d ac = new Vector2d(x1-xc, y1-yc); //pointing from c->a
			Vector2d bc = new Vector2d(x2-xc, y2-yc); //pointing from c->b
			return ac.dot(bc);
		}
		
		/** Returns a flat LineSegment with the same endpoints as this Bezier */
		public LineSegment flatten() {
			return new LineSegment(x1, y1, x2, y2);
		}
		
		@Override
		public BezierLineSegment copy() {
			return new BezierLineSegment(x1, y1, xc, yc, x2, y2);
		}
	}
	
	//public static class BezierPoint {
	//	boolean onContour;
	//	int x;
	//	int y;
	//}
	
	public static class Builder {
		private Contour result = new Contour();
		private double lastX = 0;
		private double lastY = 0;
		
		protected Builder(double x, double y) {
			lastX = x;
			lastY = y;
		}
		
		public Builder lineTo(double x, double y) {
			LineSegment segment = new LineSegment(lastX, lastY, x, y);
			result.boundary.add(segment);
			lastX = x;
			lastY = y;
			return this;
		}
		
		public Builder curveTo(double guidepointX, double guidepointY, double x, double y) {
			BezierLineSegment segment = new BezierLineSegment(lastX, lastY, guidepointX, guidepointY, x, y);
			result.boundary.add(segment);
			lastX = x;
			lastY = y;
			return this;
		}
		
		/** Builds this shape without closing it. Outlines of this shape will appear incomplete. */
		public Contour build() {
			return result;
		}
		
		/** Closes this shape with a straight line segment from the end to the beginning. */
		public Contour close() {
			if (result.boundary.isEmpty()) return result; //Can't close an empty shape with no edges
			
			//Thanks to the magic of bezier curves, these could be the same segment, and not be degenerate!
			ShapeBoundary<?> firstSegment = result.boundary.get(0);
			ShapeBoundary<?> lastSegment = result.boundary.get(result.boundary.size()-1);
			if (lastSegment.getX2()==firstSegment.getX1() && lastSegment.getY2()==firstSegment.getY1()) {
				//Skip the line since it would be degenerate
				
				if (lastSegment==firstSegment) {
					//The whole Shape is one degenerate line, throw away the geometry
					result.boundary.clear();
				}
			} else {
				LineSegment closer = new LineSegment(lastSegment.getX2(), lastSegment.getY2(), firstSegment.getX1(), firstSegment.getY1());
				result.boundary.add(closer);
			}
			return result;
		}
		
		/** Convenience method which closes this contour with a straight line as per {@link #close()}, then packages the Contour into a VectorShape. */
		public VectorShape closeIntoShape() {
			Contour contour = close();
			VectorShape result = new VectorShape(contour);
			return result;
		}
		
		/** Closes this shape with a quadratic bezier curve from the end to the beginning, using the provided guide point. */
		public Contour closeWithBezier(double guidepointX, double guidepointY) {
			if (result.boundary.isEmpty()) return result; //Can't close an empty shape with no edges
			
			//Thanks to the magic of bezier curves, these could be the same segment, and not be degenerate!
			ShapeBoundary<?> firstSegment = result.boundary.get(0);
			ShapeBoundary<?> lastSegment = result.boundary.get(result.boundary.size()-1);
			if (lastSegment.getX2()==firstSegment.getX1() && lastSegment.getY2()==firstSegment.getY1()) {
				//Skip the line since it would be degenerate
				
				if (lastSegment==firstSegment) {
					//The whole Shape is one degenerate line, throw away the geometry
					result.boundary.clear();
				}
			} else {
				BezierLineSegment closer = new BezierLineSegment(lastSegment.getX2(), lastSegment.getY2(), guidepointX, guidepointY, firstSegment.getX1(), firstSegment.getY1());
				result.boundary.add(closer);
			}
			return result;
		}
		
		public VectorShape closeWithBezierIntoShape(double guidepointX, double guidepointY) {
			Contour contour = closeWithBezier(guidepointX, guidepointY);
			VectorShape result = new VectorShape(contour);
			return result;
		}
	}
}
