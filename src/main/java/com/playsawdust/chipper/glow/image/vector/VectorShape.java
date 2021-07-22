/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.image.vector;

import java.util.ArrayList;

import org.joml.Intersectiond;
import org.joml.Matrix3d;
import org.joml.Vector2d;
import org.joml.Vector3d;

import com.playsawdust.chipper.glow.image.ImageData;
import com.playsawdust.chipper.glow.image.vector.Contour.LineSegment;
import com.playsawdust.chipper.glow.image.vector.Contour.ShapeBoundary;

public class VectorShape {
	protected double curveSegmentLengthHint = 3;
	
	protected double minX = 0;
	protected double minY = 0;
	protected double maxX = 0;
	protected double maxY = 0;
	
	protected ArrayList<Contour> contours = new ArrayList<>();
	protected ArrayList<LineSegment> approximated = new ArrayList<>();
	
	protected boolean dirty = true;
	
	public VectorShape() {}
	
	public VectorShape(Contour... contours) {
		for(Contour contour : contours) this.contours.add(contour);
	}
	
	public void addContour(Contour contour) {
		contours.add(contour);
		dirty = true;
	}
	
	protected void checkDirty() {
		if (dirty) approximate();
	}
	
	protected void approximate() {
		approximated.clear();
		
		ArrayList<LineSegment> buffer = new ArrayList<>();
		for(Contour contour : contours) {
			
			contour.getApproximation(buffer, curveSegmentLengthHint);
			if (buffer.isEmpty()) continue;
			double lastX = buffer.get(0).x1;
			double lastY = buffer.get(0).y1;
			for(LineSegment segment : buffer) {
				if (segment.x1==segment.x2 && segment.y1==segment.y2) continue;
				if (lastX!=segment.x1 || lastY!=segment.y1) {
					System.out.println("Potential gap found!");
				}
				approximated.add(segment);
				lastX = segment.x2;
				lastY = segment.y2;
			}
			buffer.clear();
		}
		
		for(LineSegment segment : approximated) {
			minX = Math.min(segment.x1, minX);
			minX = Math.min(segment.x2, minX);
			maxX = Math.max(segment.x1, maxX);
			maxX = Math.max(segment.x2, maxX);
			
			minY = Math.min(segment.y1, minY);
			minY = Math.min(segment.y2, minY);
			maxY = Math.max(segment.y1, maxY);
			maxY = Math.max(segment.y2, maxY);
		}
		
		dirty = false;
	}
	
	/**
	 * Fits the points to a supersample grid. HOWEVER, the correct way to do this is to iterate through the boundary twice, sliding a window of 3 ShapeBoundaries around the whole contour.
	 * Gridfit two points at a time along a single axis. That's way more work than I envisioned going in, so this feature is semi-abandoned.
	 */
	public void gridFit() {
		for(Contour contour : contours) {
			ShapeBoundary<?> lastBoundary = null;
			//Find the last boundary so we can wrap around
			for(ShapeBoundary<?> boundary : contour) {
				lastBoundary = boundary;
			}
			if (lastBoundary==null) continue;
			
			for (ShapeBoundary<?> boundary : contour) {
				
				if (boundary instanceof LineSegment || lastBoundary instanceof LineSegment) {
					//if (boundary.x1==boundary.x2 || boundary.y1==boundary.y2 || lastBoundary.x1==lastBoundary.x2 || lastBoundary.y1==lastBoundary.y2) {
					
						boundary.x1 = Math.floor(boundary.x1/4)*4;
						boundary.y1 = Math.floor(boundary.y1/4)*4;
						lastBoundary.x2 = boundary.x1;
						lastBoundary.y2 = boundary.y1;
					//}
				}
				
				lastBoundary = boundary;
			}
		}
		dirty = true;
	}
	
	public boolean contains(double x, double y) {
		checkDirty();
		int crossings = 0;
		for(LineSegment segment : approximated) {
			if (Intersectiond.intersectRayLineSegment(x, y, -1, 0, segment.x1, segment.y1, segment.x2, segment.y2) != -1.0) crossings++;
		}
		//think about it this way: no boundary crossings is exterior to the shape. one is interior. Two is exterior.
		return (crossings & 0x01) == 1;
	}
	
	public double distanceFromBorder(double x, double y) {
		checkDirty();
		
		double dSquared = Integer.MAX_VALUE;
		Vector3d closestPoint = new Vector3d();
		for(LineSegment segment : approximated) {
			Intersectiond.findClosestPointOnLineSegment(segment.x1, segment.y1, 0, segment.x2, segment.y2, 0, x, y, 0, closestPoint);
			double dx = closestPoint.x-x;
			double dy = closestPoint.y-y;
			double curD2 = dx*dx + dy*dy;
			dSquared = Math.min(dSquared, curD2);
			if (dSquared==0.0) return 0.0;
		}
		
		return Math.sqrt(dSquared);
	}
	
	public RectangleI getBoundingBox() {
		checkDirty();
		
		return new RectangleI((int) Math.round(minX), (int) Math.round(minY), (int) Math.ceil(maxX-minX), (int) Math.ceil(maxY-minY));
	}
	
	public void transform(Matrix3d matrix) {
		for(Contour contour : contours) {
			contour.transform(matrix);
		}
		dirty = true;
	}
	
	/*
	//TODO: BlendMode!!
	//TODO: Move this into ImageEditor?
	public void fill(ImageData image, int x, int y, int argb) {
		checkDirty();
		
		for(int yi=(int)Math.floor(minY); yi<Math.ceil(maxY); yi++) {
			for(int xi=(int)Math.floor(minX); xi<Math.ceil(maxX); xi++) {
				if (contains(xi+0.1, yi+0.1)) {
					image.setPixel(x+xi, y+yi, argb);
				}
			}
		}
	}*/
	
	public boolean isEmpty() {
		if (contours.size()==0) return true;
		else return false;
		
		//for(Contour contour : contours) if (!contour.isEmpty()) return false;
		//return true;
	}
	
	public int contourCount() {
		return contours.size();
	}
	
	public VectorShape copy() {
		VectorShape result = new VectorShape();
		for(Contour contour : contours) {
			result.contours.add(contour.copy());
		}
		result.dirty = true;
		return result;
	}
}
