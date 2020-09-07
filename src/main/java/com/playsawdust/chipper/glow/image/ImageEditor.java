package com.playsawdust.chipper.glow.image;

public interface ImageEditor {
	public static final double SRGB_GAMMA = 2.4;
	
	public static ImageEditor edit(ClientImage im) { return new ClientImageEditor(im); }
	
	public ClientImage getImage();
	
	public default void drawImage(ClientImage im, int x, int y) {
		drawImage(im, x, y, BlendMode.NORMAL);
	}
	
	public default void drawImage(ClientImage im, int x, int y, BlendMode mode) {
		for(int yi=0; yi<im.getHeight(); yi++) {
			for(int xi=0; xi<im.getWidth(); xi++) {
				paintPixel(xi+x, yi+y, im.getPixel(xi, yi), mode);
			}
		}
	}
	
	public default void paintPixel(int x, int y, int argb, BlendMode mode) {
		ClientImage dest = getImage();
		int oldPixel = dest.getPixel(x, y);
		int newPixel = BlendMode.blend(argb, oldPixel, mode);
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
