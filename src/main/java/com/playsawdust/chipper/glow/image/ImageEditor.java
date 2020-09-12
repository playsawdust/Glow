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
	}
	
	public default void fillRect(int x1, int y1, int width, int height, int argb, BlendMode mode) {
		for(int y = 0; y<height; y++) {
			for(int x = 0; x<width; x++) {
				paintPixel(x1 + x, y1 + y, argb, mode);
			}
		}
	}
}
