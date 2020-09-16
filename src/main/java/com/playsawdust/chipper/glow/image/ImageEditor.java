package com.playsawdust.chipper.glow.image;

public interface ImageEditor {
	public static final double SRGB_GAMMA = 2.4;
	
	public static ImageEditor edit(ImageData im) { return new ImageDataEditor(im); }
	
	public ImageData getImage();
	
	public default void drawImage(ImageData im, int x, int y) {
		drawImage(im, x, y, BlendMode.NORMAL);
	}
	
	public default void drawImage(ImageData im, int x, int y, BlendMode mode) {
		for(int yi=0; yi<im.getHeight(); yi++) {
			for(int xi=0; xi<im.getWidth(); xi++) {
				paintPixel(xi+x, yi+y, im.getPixel(xi, yi), mode);
			}
		}
	}
	
	public default void drawImage(ImageData im, int x, int y, BlendMode mode, double opacity) {
		for(int yi=0; yi<im.getHeight(); yi++) {
			for(int xi=0; xi<im.getWidth(); xi++) {
				paintPixel(xi+x, yi+y, im.getPixel(xi, yi), mode, opacity);
			}
		}
	}
	
	public default void drawTintImage(ImageData im, int x, int y, int tintColor, double tintStrength, BlendMode mode, double opacity) {
		tintColor |= 0xFF_000000;
		
		for(int yi=0; yi<im.getHeight(); yi++) {
			for(int xi=0; xi<im.getWidth(); xi++) {
				int srcRGB = im.getPixel(xi, yi);
				int tintRGB = BlendMode.MULTIPLY.blend(tintColor, srcRGB, tintStrength);
				//int tintRGB = BlendMode.blend(srcRGB, tintColor, BlendMode.MULTIPLY, tintStrength);
				tintRGB = tintRGB & 0xFFFFFF; //strip alpha
				tintRGB = tintRGB | (srcRGB & 0xFF_000000); //replace with srcRGB alpha
				paintPixel(xi+x, yi+y, tintRGB, mode);
			}
		}
	}
	
	public default void paintPixel(int x, int y, int argb, BlendMode mode) {
		paintPixel(x, y, argb, mode, 1.0);
	}
	
	public default void paintPixel(int x, int y, int argb, BlendMode mode, double opacity) {
		ImageData dest = getImage();
		int oldPixel = dest.getPixel(x, y);
		int newPixel = mode.blend(argb, oldPixel, opacity);
		dest.setPixel(x, y, newPixel);
	}
	
	public default void drawLine(double x1, double y1, double x2, double y2, int argb, BlendMode mode) {
		double dx = x2-x1;
		double dy = y2-y1;
		double scale = Math.max(Math.abs(dx), Math.abs(dy));
		if(scale == 0) return; //Degenerate line skipped so we don't DivideByZero
		dx /= scale;
		dy /= scale;
		
		double xi = x1;
		double yi = y1;
		for(int i=0; i<(int)scale; i++) {
			paintPixel((int)xi, (int)yi, argb, mode);
			xi += dx;
			yi += dy;
		}
		paintPixel((int)xi, (int)yi, argb, mode);
	}
	
	
	public default void drawQuadraticCurve(double x1, double y1, double x2, double y2, double x3, double y3, int precision, int argb, BlendMode mode) {
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
	
	
	public default void fillRect(int x1, int y1, int width, int height, int argb, BlendMode mode) {
		for(int y = 0; y<height; y++) {
			for(int x = 0; x<width; x++) {
				paintPixel(x1 + x, y1 + y, argb, mode);
			}
		}
	}
}
