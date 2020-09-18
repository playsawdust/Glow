package com.playsawdust.chipper.glow.util;

import java.util.ArrayList;

import org.joml.Intersectiond;
import org.joml.Matrix3d;

import com.playsawdust.chipper.glow.image.ImageData;
import com.playsawdust.chipper.glow.util.Contour.LineSegment;

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
		for(Contour contour : contours) contour.getApproximation(approximated, curveSegmentLengthHint);
		
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
	
	public boolean contains(double x, double y) {
		checkDirty();
		int crossings = 0;
		for(LineSegment segment : approximated) {
			if (Intersectiond.intersectRayLineSegment(x, y, -1, 0, segment.x1, segment.y1, segment.x2, segment.y2) != -1.0) crossings++;
		}
		//think about it this way: no boundary crossings is exterior to the shape. one is interior. Two is exterior.
		return (crossings & 0x01) == 1;
	}
	
	public void transform(Matrix3d matrix) {
		for(Contour contour : contours) {
			contour.transform(matrix);
		}
		dirty = true;
	}
	
	//TODO: BlendMode!!
	//TODO: Move this into ImageEditor?
	public void fill(ImageData image, int x, int y, int argb) {
		checkDirty();
		
		for(int yi=(int)Math.floor(minY); yi<Math.ceil(maxY); yi++) {
			for(int xi=(int)Math.floor(minX); xi<Math.ceil(maxX); xi++) {
				if (contains(xi+0.5, yi+0.5)) {
					image.setPixel(x+xi, y+yi, argb);
				}
			}
		}
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
