package com.playsawdust.chipper.glow.scene;

import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Collision {
	/**
	 * Finds the point along a line segment defined by `a` and `b` which is closest to `p`, and stores the value in result.
	 * @return result, which now contains the point in the line segment `ab` which is closest to `p`
	 */
	public static Vector3d closestSegmentPointToPoint(Vector3dc a, Vector3dc b, Vector3dc p, Vector3d result) {
		
		//Create a unit vector pointing from `a` towards `b`
		Vector3d aToB = new Vector3d();
		aToB.set(b);
		aToB.sub(a);
		double segmentLength = aToB.length(); //Grab out the magnitude of this vector before we zap it
		aToB.normalize();
		
		//Create a NON-normalized vector pointing from `a` towards `p`, containing their distance as the magnitude
		Vector3d aToP = new Vector3d();
		aToP.set(p);
		aToP.sub(a);
		
		//Project aToP onto aToB, creating a scalar length across aToB which is how far we need to travel to hit the desired intersection point
		double travelDistance = aToB.dot(aToP);
		
		if (travelDistance<=0) {
			//the projected point would be "behind" a, outside the line, so we return a.
			result.set(a);
			return result;
		} else if (travelDistance>=segmentLength) {
			//the projected point would "overshoot" b, outside the line, so we return b.
			result.set(b);
			return result;
		} else {
			//The projected point will lie on the line segment so do the projection and return it.
			result.set(aToB);
			result.mul(travelDistance);
			result.add(a);
			return result;
		}
	}
	
	/**
	 * Finds the point within the triangle `abc` which is closest to the sphere defined by `center` and `radius`. Points must be specified
	 * in counter-clockwise order.
	 * @param q The center of the sphere
	 * @param r The sphere radius
	 * @param a A corner of the triangle
	 * @param b A corner of the triangle
	 * @param c A corner of the triangle
	 * @param result Set to the closest point in the triangle `abc` to the sphere.
	 * @return the result argument
	 */
	public static Vector3d closestTrianglePointToSphere(Vector3dc q, double r, Vector3dc a, Vector3dc b, Vector3dc c, Vector3d result) {
		Vector3d ac = new Vector3d(c).sub(a); //ordering intentional
		Vector3d ab = new Vector3d(b).sub(a);
		
		Vector3d normal = new Vector3d(ac).cross(ab).normalize();
		
		//Project the line from a->q onto the normal vector, which gives us the distance from the sphere to the plane the triangle sits on
		Vector3d aq = new Vector3d(q).sub(a);
		double distance = new Vector3d(aq).dot(normal);
		
		//if (distance < -r || distance > r) there's no intersection so we could bail here if we didn't need the closest point
		
		//Do the actual point projection from the sphere center to the plane
		Vector3d s = new Vector3d(q).sub(new Vector3d(normal).mul(distance));
		
		//This gets complicated: pretend that a->q and a->b are basis vectors, find the normal(crossproduct).
		//Because of winding, this will be in the normal's direction if it falls within the triangle-side of the line, and out the back of the triangle if not.
		//We need to check all three a->q a->b / b->q b->c / c->q c->a to know whether it's inside the triangle as a whole
		Vector3d bc = new Vector3d(c).sub(b);
		Vector3d ca = new Vector3d(a).sub(c);
		Vector3d bq = new Vector3d(q).sub(b);
		Vector3d cq = new Vector3d(q).sub(c);
		
		Vector3d aqabNormal = new Vector3d(aq).cross(ab);
		Vector3d bqbcNormal = new Vector3d(bq).cross(bc);
		Vector3d cqcaNormal = new Vector3d(cq).cross(ca);
		
		boolean inside = aqabNormal.dot(normal)<=0 && bqbcNormal.dot(normal)<=0 && cqcaNormal.dot(normal)<=0;
		
		if (inside) {
			//The projected point is *in* the triangle, so that's our closest point
			result.set(s);
			return result;
		} else {
			//The projected point is *outside* the triangle, so we need to find the closest point on each edge, and then pick the closest one of those.
			Vector3d abClosest = closestSegmentPointToPoint(a, b, s, new Vector3d());
			Vector3d bcClosest = closestSegmentPointToPoint(b, c, s, new Vector3d());
			Vector3d caClosest = closestSegmentPointToPoint(c, a, s, new Vector3d());
			
			double abDistance = abClosest.distance(s);
			double bcDistance = bcClosest.distance(s);
			double caDistance = caClosest.distance(s);
			
			if (abDistance<=bcDistance && abDistance<=caDistance) {
				result.set(abClosest);
				return result;
			} else if (bcDistance<=abDistance && bcDistance<=caDistance) {
				result.set(bcClosest);
				return result;
			} else {
				result.set(caClosest);
				return result;
			}
		}
	}
}
