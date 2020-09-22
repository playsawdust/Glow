package com.playsawdust.chipper.glow.image;

import java.util.Arrays;

import org.joml.Vector2d;

import com.playsawdust.chipper.glow.event.Vector2dEvent;
import com.playsawdust.chipper.glow.util.MathUtil;
import com.playsawdust.chipper.glow.util.RectangleI;
import com.playsawdust.chipper.glow.util.VectorShape;

public class ImageEditor {
	public static final double SRGB_GAMMA = 2.4;
	
	private ImageData dest;
	
	public ImageEditor(ImageData image) {
		this.dest = image;
	}
	
	public static ImageEditor edit(ImageData im) { return new ImageEditor(im); }
	
	public void drawImage(ImageData im, int x, int y) {
		drawImage(im, x, y, BlendMode.NORMAL);
	}
	
	public void drawImage(ImageData im, int x, int y, BlendMode mode) {
		for(int yi=0; yi<im.getHeight(); yi++) {
			for(int xi=0; xi<im.getWidth(); xi++) {
				drawPixel(xi+x, yi+y, im.getPixel(xi, yi), mode);
			}
		}
	}
	
	public void drawImage(ImageData im, int x, int y, BlendMode mode, double opacity) {
		for(int yi=0; yi<im.getHeight(); yi++) {
			for(int xi=0; xi<im.getWidth(); xi++) {
				drawPixel(xi+x, yi+y, im.getPixel(xi, yi), mode, opacity);
			}
		}
	}
	
	public void drawTintImage(ImageData im, int x, int y, int tintColor, double tintStrength, BlendMode mode, double opacity) {
		tintColor |= 0xFF_000000;
		
		for(int yi=0; yi<im.getHeight(); yi++) {
			for(int xi=0; xi<im.getWidth(); xi++) {
				int srcRGB = im.getPixel(xi, yi);
				int tintRGB = BlendMode.MULTIPLY.blend(tintColor, srcRGB, tintStrength);
				//int tintRGB = BlendMode.blend(srcRGB, tintColor, BlendMode.MULTIPLY, tintStrength);
				tintRGB = tintRGB & 0xFFFFFF; //strip alpha
				tintRGB = tintRGB | (srcRGB & 0xFF_000000); //replace with srcRGB alpha
				drawPixel(xi+x, yi+y, tintRGB, mode);
			}
		}
	}
	
	public void drawPixel(int x, int y, int argb, BlendMode mode) {
		drawPixel(x, y, argb, mode, 1.0);
	}
	
	public void drawPixel(int x, int y, int argb, BlendMode mode, double opacity) {
		int oldPixel = dest.getPixel(x, y);
		int newPixel = mode.blend(argb, oldPixel, opacity);
		dest.setPixel(x, y, newPixel);
	}
	
	public void drawLine(double x1, double y1, double x2, double y2, int argb, BlendMode mode) {
		double dx = x2-x1;
		double dy = y2-y1;
		double scale = Math.max(Math.abs(dx), Math.abs(dy));
		if(scale == 0) return; //Degenerate line skipped so we don't DivideByZero
		dx /= scale;
		dy /= scale;
		
		double xi = x1;
		double yi = y1;
		for(int i=0; i<(int)scale; i++) {
			drawPixel((int)xi, (int)yi, argb, mode);
			xi += dx;
			yi += dy;
		}
		drawPixel((int)xi, (int)yi, argb, mode);
	}
	
	public void drawLine(double x1, double y1, double x2, double y2, int argb, BlendMode mode, double lineWeight) {
		if (Math.abs(x2-x1)==0 && Math.abs(y2-y1)==0) return; //Degenerate line
		
		Vector2d ab = new Vector2d(x2-x1, y2-y1);
		ab.normalize();
		
		Vector2d normal = new Vector2d(ab.y, -ab.x);
		normal.mul(lineWeight/2.0);
		System.out.println(normal);
		
		//int debugColor3 = 0x66_FF00FF;
		
		fillQuad(
				(int) (x1-normal.x), (int) (y1-normal.y),
				(int) (x1+normal.x), (int) (y1+normal.y),
				(int) (x2+normal.x), (int) (y2+normal.y),
				(int) (x2-normal.x), (int) (y2-normal.y),
				argb, mode
				);
	}
	
	
	public void drawQuadraticCurve(double x1, double y1, double x2, double y2, double x3, double y3, int precision, int argb, BlendMode mode) {
		double stepSize = 1.0 / precision;
		double t = 0;
		double lastX = x1;
		double lastY = y1;
		while(t<1) {
			double x = quadratic(x1, x2, x3, t);
			double y = quadratic(y1, y2, y3, t);
			drawLine(lastX, lastY, x, y, argb, mode);
			
			t+= stepSize;
			lastX = x;
			lastY = y;
		}
		drawLine(lastX, lastY, x3, y3, argb, mode);
	}
	
	//Used by drawQuadraticCurve to calculate quadratic bezier coordinates
	private static double quadratic(double a, double b, double c, double t) {
		double aTerm = c * Math.pow(t, 2);
		double bTerm = b * 2 * t * (1 - t);
		double cTerm = a * Math.pow((1 - t), 2);
		
		return aTerm + bTerm + cTerm;
	}
	
	
	public void fillRect(int x, int y, int width, int height, int argb, BlendMode mode) {
		for(int yi = 0; yi<height; yi++) {
			for(int xi = 0; xi<width; xi++) {
				drawPixel(x + xi, y + yi, argb, mode);
			}
		}
	}
	
	public void outlineRect(int x, int y, int width, int height, int argb, BlendMode mode, double lineWeight) {
		int full = (int)lineWeight;
		if (full==0) full=1;
		int above = (int) (lineWeight / 2.0);
		int below = full - above;
		
		
		//top
		fillRect(x-above, y-above, width+full, full, argb, mode);
		//bottom
		fillRect(x-above, y+height-above, width+full, full, argb, mode);
		//left - withdraw the top and bottom edge to not collide with top or bottom
		fillRect(x-above, y+below, full, height-full, argb, mode);
		//right - " "
		fillRect(x+width-above, y+below, full, height-full, argb, mode);
	}
	
	public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int argb, BlendMode mode) {
		//int minX = MathUtil.min(x1, x2, x3);
		//int maxX = MathUtil.max(x1, x2, x3);
		int minY = MathUtil.min(y1, y2, y3);
		int maxY = MathUtil.max(y1, y2, y3);
		
		//int dx = maxX-minX;
		int dy = maxY-minY;
		
		int[] xStart = new int[dy];
		int[] xEnd = new int[dy];
		Arrays.fill(xStart, Integer.MAX_VALUE);
		Arrays.fill(xEnd, -1);
		
		bresenham(x1,y1,x2,y2, (x,y)->{
			int yi = ((int) y) - minY;
			if (yi<0 || yi>=dy) return;
			
			xStart[yi] = Math.min(xStart[yi], (int) x);
			xEnd[yi] = Math.max(xEnd[yi], (int) x);
		});
		
		bresenham(x2,y2,x3,y3, (x,y)->{
			int yi = ((int) y) - minY;
			if (yi<0 || yi>=dy) return;
			
			xStart[yi] = Math.min(xStart[yi], (int) x);
			xEnd[yi] = Math.max(xEnd[yi], (int) x);
		});
		
		bresenham(x3,y3,x1,y1, (x,y)->{
			int yi = ((int) y) - minY;
			if (yi<0 || yi>=dy) return;
			
			xStart[yi] = Math.min(xStart[yi], (int) x);
			xEnd[yi] = Math.max(xEnd[yi], (int) x);
		});
		
		for(int y=0; y<dy; y++) {
			if (xEnd[y]>xStart[y]) {
				for(int x=xStart[y]; x<=xEnd[y]; x++) {
					drawPixel(x, y+minY, argb, mode);
				}
			}
		}
	}
	
	public void fillQuad(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, int argb, BlendMode mode) {
		//int minX = MathUtil.min(x1, x2, x3, x4);
		//int maxX = MathUtil.max(x1, x2, x3, x4);
		int minY = MathUtil.min(y1, y2, y3, y4);
		int maxY = MathUtil.max(y1, y2, y3, y4);
		
		//int dx = maxX-minX;
		int dy = maxY-minY;
		
		int[] xStart = new int[dy];
		int[] xEnd = new int[dy];
		Arrays.fill(xStart, Integer.MAX_VALUE);
		Arrays.fill(xEnd, -1);
		
		bresenham(x1,y1,x2,y2, (x,y)->{
			int yi = ((int) y) - minY;
			if (yi<0 || yi>=dy) return;
			
			xStart[yi] = Math.min(xStart[yi], (int) x);
			xEnd[yi] = Math.max(xEnd[yi], (int) x);
		});
		
		bresenham(x2,y2,x3,y3, (x,y)->{
			int yi = ((int) y) - minY;
			if (yi<0 || yi>=dy) return;
			
			xStart[yi] = Math.min(xStart[yi], (int) x);
			xEnd[yi] = Math.max(xEnd[yi], (int) x);
		});
		
		bresenham(x3,y3,x4,y4, (x,y)->{
			int yi = ((int) y) - minY;
			if (yi<0 || yi>=dy) return;
			
			xStart[yi] = Math.min(xStart[yi], (int) x);
			xEnd[yi] = Math.max(xEnd[yi], (int) x);
		});
		
		bresenham(x4,y4,x1,y1, (x,y)->{
			int yi = ((int) y) - minY;
			if (yi<0 || yi>=dy) return;
			
			xStart[yi] = Math.min(xStart[yi], (int) x);
			xEnd[yi] = Math.max(xEnd[yi], (int) x);
		});
		
		for(int y=0; y<dy; y++) {
			if (xEnd[y]>xStart[y]) {
				for(int x=xStart[y]; x<=xEnd[y]; x++) {
					drawPixel(x, y+minY, argb, mode);
				}
			}
		}
	}
	
	public void fillCircle(int x, int y, double radius, int argb, BlendMode mode) {
		int ir = (int) Math.ceil(radius);
		double r2 = radius * radius;
		
		for(int iy=y-ir; iy<=y+ir; iy++) {
			for(int ix=x-ir; ix<=x+ir; ix++) {
				double dx = ix-x;
				double dy = iy-y;
				double d = dx*dx + dy*dy;
				if (d<r2) drawPixel(ix, iy, argb, mode);
			}
		}
	}
	
	public void outlineCircle(int x, int y, double radius, int argb, BlendMode mode, double weight) {
		weight = weight / 2.0;
		int ir = (int) Math.ceil(radius);
		//double r2 = radius * radius;
		double innerRadius = (radius-weight) * (radius-weight);
		double outerRadius = (radius+weight) * (radius+weight);
		
		for(int iy=y - ir - (int) radius; iy<=y + ir + (int) radius; iy++) {
			for(int ix=x - ir - (int) radius; ix<=x + ir + (int) radius; ix++) {
				double dx = ix-x;
				double dy = iy-y;
				double d = dx*dx + dy*dy;
				if (d>=innerRadius && d<=outerRadius) drawPixel(ix, iy, argb, mode);
			}
		}
	}
	
	public void fillShape(VectorShape shape, int x, int y, int argb, BlendMode mode) {
		RectangleI bounds = shape.getBoundingBox();
		for(int yi=bounds.y(); yi<bounds.y()+bounds.height(); yi++) {
			for(int xi=bounds.x(); xi<bounds.x()+bounds.width(); xi++) {
				if (shape.contains(xi+0.1, yi+0.1)) {
					drawPixel(xi+x, yi+y, argb, mode);
				}
			}
		}
	}
	
	public void outlineShape(VectorShape shape, int x, int y, int argb, BlendMode mode, double weight) {
		weight /= 2.0;
		
		RectangleI bounds = shape.getBoundingBox();
		for(int yi=bounds.y()-(int)Math.ceil(weight); yi<bounds.y()+bounds.height()+(int)Math.ceil(weight); yi++) {
			for(int xi=bounds.x()-(int)Math.ceil(weight); xi<bounds.x()+bounds.width()+(int)Math.ceil(weight); xi++) {
				double dist = shape.distanceFromBorder(xi+0.1, yi+0.1);
				if (dist<weight) drawPixel(xi+x, yi+y, argb, mode);
			}
		}
	}
	
	
	protected void bresenham(double x1, double y1, double x2, double y2, Vector2dEvent.Handler consumer) {
		double dx = x2-x1;
		double dy = y2-y1;
		double scale = Math.max(Math.abs(dx), Math.abs(dy));
		if(scale == 0) return; //Degenerate line skipped so we don't DivideByZero
		dx /= scale;
		dy /= scale;
		
		double xi = x1;
		double yi = y1;
		for(int i=0; i<(int)scale; i++) {
			consumer.accept(xi, yi);
			xi += dx;
			yi += dy;
		}
		consumer.accept(xi, yi);
	}
}
