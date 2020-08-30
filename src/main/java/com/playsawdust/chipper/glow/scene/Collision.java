package com.playsawdust.chipper.glow.scene;

import java.util.ArrayList;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.playsawdust.chipper.glow.mesher.VoxelMesher;
import com.playsawdust.chipper.glow.voxel.VecFunction;
import com.playsawdust.chipper.glow.voxel.VoxelShape;

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
	 * For any two non-parallel, non-intersecting line segments A and B, returns the unique shortest-length line segment which connects a point which lies on A to a point which lies on B.
	 * For intersecting line segments A and B, returns the unique zero-length segment defining their intersection. For parallel segments A and B, returns one of the minimum-length line
	 * segments which connect them.
	 * @param a1 First point on segment A
	 * @param a2 Second point on segment A
	 * @param b1 First point on segment B
	 * @param b2 Second point on segment B
	 * @param aResult Will be set to the point on segment A which is closest to segment B
	 * @param bResult Will be set to the point on segment B which is closest to segment A
	 */
	public static void closestPointsOnTwoSegments(Vector3dc a1, Vector3dc a2, Vector3dc b1, Vector3dc b2, Vector3d aResult, Vector3d bResult) {
		//TODO: Special-case certain parallel segment cases where there are several correct answers, to return the middle answer if possible.
		//TODO: Benchmark this against repeated sampling and possibly binary search to try and find the discontinuous minimum, which will produce more reliable results
		
		//Find the squared distances to each pair of endpoints
		double dA1B1 = b1.distanceSquared(a1);
		double dA1B2 = b2.distanceSquared(a1);
		double dA2B1 = b1.distanceSquared(a2);
		double dA2B2 = b2.distanceSquared(a2);
		
		//Pick the closest two endpoints, and save the endpoint from segment A from those two.
		double min = Math.min(Math.min(dA1B1, dA1B2), Math.min(dA2B1, dA2B2));
		Vector3dc aP = a1;
		if (min==dA1B1) {
			aP = a1;
		} else if (min==dA1B2) {
			aP = a1;
		} else if (min==dA2B1) {
			aP = a2;
		} else { //min==dA2B2
			aP = a2;
		}
		
		//Find the point on B which is closest to this "near" endpoint of A that we called aP. This new point on B we call bResult.
		closestSegmentPointToPoint(b1, b2, aP, bResult);
		//Find the point on A which is closest to the bResult point. This new point on A we call aResult.
		closestSegmentPointToPoint(a1, a2, bResult, aResult);
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
	
	public static @Nullable Vector3d raycastVoxelCoarse(Vector3dc start, Vector3dc ray, double limit, VecFunction<VoxelShape> getVoxelShape) {
		double len = 0;
		double stepMagnitude = 0.25;
		Vector3d cur = new Vector3d(start);
		Vector3d step = new Vector3d(ray).mul(stepMagnitude);
		while(len<limit) {
			VoxelShape shape = getVoxelShape.apply((int)cur.x, (int)cur.y, (int)cur.z);
			if (shape!=VoxelShape.EMPTY) {
				return cur;
			}
			
			cur.add(step);
			len+=stepMagnitude;
		}
		return null;
	}
	
	/** Verbose collisions include stepping information across the ray */
	public static @Nullable Vector3d raycastVoxel(Vector3dc start, Vector3dc ray, double limit, VecFunction<VoxelShape> getVoxelShape, @Nullable CollisionResult result, boolean verbose) {
		if (result!=null) result.clearSteps();
		double len = 0;
		Vector3d cur = new Vector3d(start);
		
		ArrayList<Vector3dc> planes = new ArrayList<>();
		double dx = ray.x();
		double dy = ray.y();
		double dz = ray.z();
		double mdx = Math.abs(dx);
		double mdy = Math.abs(dy);
		double mdz = Math.abs(dz);
		
		//Add vectors representing the complement of the normals for the three planes we might collide with, favoring the axis the ray is traveling in
		if (mdz>=mdx && mdz>=mdy) {
			planes.add(dz<0 ? VoxelMesher.VEC_ZMINUS : VoxelMesher.VEC_ZPLUS);
			planes.add(dx<0 ? VoxelMesher.VEC_XMINUS : VoxelMesher.VEC_XPLUS);
			planes.add(dy<0 ? VoxelMesher.VEC_YMINUS : VoxelMesher.VEC_YPLUS);
		}  else if (mdy>=mdx && mdy>=mdz) {
			planes.add(dy<0 ? VoxelMesher.VEC_YMINUS : VoxelMesher.VEC_YPLUS);
			planes.add(dx<0 ? VoxelMesher.VEC_XMINUS : VoxelMesher.VEC_XPLUS);
			planes.add(dz<0 ? VoxelMesher.VEC_ZMINUS : VoxelMesher.VEC_ZPLUS);
		} else {
			planes.add(dx<0 ? VoxelMesher.VEC_XMINUS : VoxelMesher.VEC_XPLUS);
			planes.add(dy<0 ? VoxelMesher.VEC_YMINUS : VoxelMesher.VEC_YPLUS);
			planes.add(dz<0 ? VoxelMesher.VEC_ZMINUS : VoxelMesher.VEC_ZPLUS);
		}
		
		int cubeX = (int)cur.x;
		int cubeY = (int)cur.y;
		int cubeZ = (int)cur.z;
		
		if ( (dx<0) && (Math.floor(cur.x)==cur.x) ) cubeX--;
		if ( (dy<0) && (Math.floor(cur.y)==cur.y) ) cubeY--;
		if ( (dz<0) && (Math.floor(cur.z)==cur.z) ) cubeZ--;
		
		Vector3d proj = new Vector3d();
		while(len<limit) {
			boolean failure = true;
			for(Vector3dc vec : planes) {
				if (vec==VoxelMesher.VEC_YPLUS) {
					Vector3d projB = rayPlane(cur, ray, new Vector3d(0, cubeY+1, 0), new Vector3d(vec).mul(-1), proj);
					//Vector3d projB = rayPlaneY(cur, ray, cubeY+1, proj);
					if (projB!=null) {
						
						if ((projB.x>=cubeX && projB.x<=cubeX+1) && (projB.z>=cubeZ && projB.z<=cubeZ+1)) {
							double crossing = new Vector3d(projB).sub(cur).length(); //How far did we go during this projection?
							len += crossing;
							if (result!=null && verbose) result.addStep(new Vector3d(projB));
							//grab the target cube to see if we collided
							VoxelShape shape = getVoxelShape.apply(cubeX, cubeY+1, cubeZ);
							if (shape!=VoxelShape.EMPTY) {
								if (result!=null) {
									result.setVoxelPos(cubeX, cubeY+1, cubeZ);
									result.setHitLocation(projB);
									result.setHitNormal(new Vector3d(0, -1, 0));
								}
								
								return projB;
							}
							failure = false;
							cur.set(projB);
							cubeY += 1;
							break;
						}
					}
				} else if (vec==VoxelMesher.VEC_YMINUS) {
					Vector3d projB = rayPlane(cur, ray, new Vector3d(0, cubeY, 0), new Vector3d(vec).mul(-1), proj);
					//Vector3d projB = rayPlaneY(cur, ray, cubeY, proj);
					if (projB!=null) {
						
						if ((projB.x>=cubeX && projB.x<=cubeX+1) && (projB.z>=cubeZ && projB.z<=cubeZ+1)) {
							double crossing = new Vector3d(projB).sub(cur).length(); //How far did we go during this projection?
							len += crossing;
							if (result!=null && verbose) result.addStep(new Vector3d(projB));
							//grab the target cube to see if we collided
							VoxelShape shape = getVoxelShape.apply(cubeX, cubeY-1, cubeZ);
							if (shape!=VoxelShape.EMPTY) {
								if (result!=null) {
									result.setVoxelPos(cubeX, cubeY-1, cubeZ);
									result.setHitLocation(projB);
									result.setHitNormal(new Vector3d(0, 1, 0));
								}
								
								return projB;
							}
							failure = false;
							cur.set(projB);
							cubeY -= 1;
							break;
						}
					}
				} else if (vec==VoxelMesher.VEC_XPLUS) {
					Vector3d projB = rayPlane(cur, ray, new Vector3d(cubeX+1, 0, 0), new Vector3d(vec).mul(-1), proj);
					//Vector3d projB = rayPlaneX(cur, ray, cubeX+1, proj);
					if (projB!=null) {
						
						if ((projB.y>=cubeY && projB.y<=cubeY+1) && (projB.z>=cubeZ && projB.z<=cubeZ+1)) {
							double crossing = new Vector3d(projB).sub(cur).length(); //How far did we go during this projection?
							len += crossing;
							if (result!=null && verbose) result.addStep(new Vector3d(projB));
							//grab the target cube to see if we collided
							VoxelShape shape = getVoxelShape.apply(cubeX+1, cubeY, cubeZ);
							if (shape!=VoxelShape.EMPTY) {
								if (result!=null) {
									result.setVoxelPos(cubeX+1, cubeY, cubeZ);
									result.setHitLocation(projB);
									result.setHitNormal(new Vector3d(-1, 0, 0));
								}
								
								return projB;
							}
							failure = false;
							cur.set(projB);
							cubeX += 1;
							break;
						}
					}
				} else if (vec==VoxelMesher.VEC_XMINUS) {
					Vector3d projB = rayPlane(cur, ray, new Vector3d(cubeX, 0, 0), new Vector3d(vec).mul(-1), proj);
					//Vector3d projB = rayPlaneX(cur, ray, cubeX, proj);
					if (projB!=null) {
						
						if ((projB.y>=cubeY && projB.y<=cubeY+1) && (projB.z>=cubeZ && projB.z<=cubeZ+1)) {
							double crossing = new Vector3d(projB).sub(cur).length(); //How far did we go during this projection?
							len += crossing;
							if (result!=null && verbose) result.addStep(new Vector3d(projB));
							//grab the target cube to see if we collided
							VoxelShape shape = getVoxelShape.apply(cubeX-1, cubeY, cubeZ);
							if (shape!=VoxelShape.EMPTY) {
								if (result!=null) {
									result.setVoxelPos(cubeX-1, cubeY, cubeZ);
									result.setHitLocation(projB);
									result.setHitNormal(new Vector3d(1, 0, 0));
								}
								
								return projB;
							}
							failure = false;
							cur.set(projB);
							cubeX -= 1;
							break;
						}
					}
				} else if (vec==VoxelMesher.VEC_ZPLUS) {
					Vector3d projB = rayPlane(cur, ray, new Vector3d(0, 0, cubeZ+1), new Vector3d(vec).mul(-1), proj);
					//Vector3d projB = rayPlaneZ(cur, ray, cubeZ+1, proj);
					if (projB!=null) {
						
						if ((projB.x>=cubeX && projB.x<=cubeX+1) && (projB.y>=cubeY && projB.y<=cubeY+1)) {
							double crossing = new Vector3d(projB).sub(cur).length(); //How far did we go during this projection?
							len += crossing;
							if (result!=null && verbose) result.addStep(new Vector3d(projB));
							//grab the target cube to see if we collided
							VoxelShape shape = getVoxelShape.apply(cubeX, cubeY, cubeZ+1);
							if (shape!=VoxelShape.EMPTY) {
								if (result!=null) {
									result.setVoxelPos(cubeX, cubeY, cubeZ+1);
									result.setHitLocation(projB);
									result.setHitNormal(new Vector3d(0, 0, -1));
								}
								
								return projB;
							}
							failure = false;
							cur.set(projB);
							cubeZ += 1;
							break;
						}
					}
				} else if (vec==VoxelMesher.VEC_ZMINUS) {
					Vector3d projB = rayPlane(cur, ray, new Vector3d(0, 0, cubeZ), new Vector3d(vec).mul(-1), proj);
					//Vector3d projB = rayPlaneZ(cur, ray, cubeZ, proj);
					if (projB!=null) {
						
						if ((projB.x>=cubeX && projB.x<=cubeX+1) && (projB.y>=cubeY && projB.y<=cubeY+1)) {
							if (result!=null && verbose) result.addStep(new Vector3d(projB));
							double crossing = new Vector3d(projB).sub(cur).length(); //How far did we go during this projection?
							len += crossing;
							//grab the target cube to see if we collided
							VoxelShape shape = getVoxelShape.apply(cubeX, cubeY, cubeZ-1);
							if (shape!=VoxelShape.EMPTY) {
								if (result!=null) {
									result.setVoxelPos(cubeX, cubeY, cubeZ-1);
									result.setHitLocation(projB);
									result.setHitNormal(new Vector3d(0, 0, 1));
								}
								
								return projB;
							}
							failure = false;
							cur.set(projB);
							cubeZ -= 1;
							break;
						}
					}
				}
			}
			
			if (failure) {
				return null; //Rebellion 2.0 You Can Not Advance
			}
			
		}
		return null;
	}
	
	
	public static Vector3d rayPlane(Vector3dc start, Vector3dc ray, Vector3dc planePoint, Vector3dc planeNormal, Vector3d result) {
		double denominator = planeNormal.dot(ray);
		if (denominator==0) return null; //Ray is parallel to plane
		double numerator = new Vector3d(planePoint).sub(start).dot(planeNormal);
		
		double distance = numerator/denominator;
		if (distance<=0) return null; //Ray diverges
		
		result.set(ray).mul(distance).add(start);
		return result;
	}
	
	
	/*
	
	
	public static Vector3d rayPlaneY(Vector3dc start, Vector3dc ray, double y, Vector3d result) {
		//Does start lie on the plane? If so we immediately strike the plane.
		if (y==start.y()) {
			result.set(start);
			return result;
		}
		
		//Is the ray not on the plane, but parallel to it? If so we can never strike the plane.
		if (ray.y()==0) return null;
		
		//Will we always diverge from the plane?
		if (ray.y()>0 && y<start.y()) return null;
		if (ray.y()<0 && y>start.y()) return null;
		
		
		Vector3d planeNormal = new Vector3d(1, 0, 0);
		double denominator = planeNormal.dot(ray);
		if (denominator==0) return null; //Probably superfluous because we already check for parallel ray above
		double numerator = new Vector3d(0, y, 0).sub(start).dot(planeNormal);
		
		double distance = numerator/denominator;
		if (distance<=0) return null;
		
		//And the Z axis?
		//planeNormal.set(0, 0, 1);
		//double zResult = planeNormal.dot(ray);
	
		//result.set(start.x()+xResult, y, start.z()+zResult);
		result.set(ray).mul(distance).add(start);
		return result;
	}
	

	public static Vector3d rayPlaneX(Vector3dc start, Vector3dc ray, double x, Vector3d result) {
		Vector3d longRay = new Vector3d(ray).mul(Math.abs(x-start.x()));
		
		//Does start lie on the plane? If so we immediately strike the plane.
		if (x==start.x()) {
			result.set(start);
			return result;
		}
		
		//Is the ray not on the plane, but parallel to it? If so we can never strike the plane.
		if (ray.x()==0) return null;
		
		//Will we always diverge from the plane?
		if (ray.x()>0 && x<start.x()) return null;
		if (ray.x()<0 && x>start.x()) return null;
		
		//We will, at some point, strike the plane. Where on the Y axis?
		Vector3d projection = new Vector3d(0, 1, 0);
		double yResult = projection.dot(longRay);
		//And the Z axis?
		projection.set(0, 0, 1);
		double zResult = projection.dot(longRay);
	
		result.set(x, start.y()+yResult, start.z()+zResult);
		return result;
	}
	

	public static Vector3d rayPlaneZ(Vector3dc start, Vector3dc ray, double z, Vector3d result) {
		Vector3d longRay = new Vector3d(ray).mul(Math.abs(z-start.z()));
		
		//Does start lie on the plane? If so we immediately strike the plane.
		if (z==start.z()) {
			result.set(start);
			return result;
		}
		
		//Is the ray not on the plane, but parallel to it? If so we can never strike the plane.
		if (ray.z()==0) return null;
		
		//Will we always diverge from the plane?
		if (ray.z()>0 && z<start.z()) return null;
		if (ray.z()<0 && z>start.z()) return null;
		
		//We will, at some point, strike the plane. Where on the X axis?
		Vector3d projection = new Vector3d(1, 0, 0);
		double xResult = projection.dot(longRay);
		//And the Y axis?
		projection.set(0, 1, 0);
		double yResult = projection.dot(longRay);
	
		result.set(start.x()+xResult, start.y()+yResult, z);
		return result;
	}*/
}
