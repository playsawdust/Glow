package com.playsawdust.chipper.glow.scene;

import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Collision {
	/**
	 * Finds the point along a line segment defined by `a` and `b` which is closest to `p`, and stores the value in result.
	 * @return result, which now contains the point in the line segment `ab` which is closest to `p`
	 */
	public static Vector3d closestPointOnSegment(Vector3dc a, Vector3dc b, Vector3dc p, Vector3d result) {
		
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
}
