package com.playsawdust.chipper.glow.image;

public interface ImageEditor {
	public static final double SRGB_GAMMA = 2.4;
	
	public static ImageEditor edit(ClientImage im) { return new ClientImageEditor(im); }
	
	public ClientImage getImage();
	
	public default void drawImage(ClientImage im, int x, int y) {
		for(int yi=0; yi<im.getHeight(); yi++) {
			for(int xi=0; xi<im.getWidth(); xi++) {
				paintPixel(xi+x, yi+y, im.getPixel(xi, yi));
			}
		}
	}
	
	public default void paintPixel(int x, int y, int argb) {
		ClientImage dest = getImage();
		int oldPixel = dest.getPixel(x, y);
		int newPixel = compositeARGB(argb, oldPixel);
		dest.setPixel(x, y, newPixel);
	}
	
	public static int compositeARGB(int src, int dest) {
		int sa = (src >> 24) & 0xFF;
		int sr = (src >> 16) & 0xFF;
		int sg = (src >>  8) & 0xFF;
		int sb = (src      ) & 0xFF;
		
		int da = (dest >> 24) & 0xFF;
		int dr = (dest >> 16) & 0xFF;
		int dg = (dest >>  8) & 0xFF;
		int db = (dest      ) & 0xFF;
		
		double srcAlpha = sa / 255.0;
		double destAlpha = da / 255.0;
		double outAlpha = srcAlpha + destAlpha*(1-srcAlpha);
		
		double r = compositeElement(gammaToLinear(sr), gammaToLinear(dr), srcAlpha, destAlpha, outAlpha);
		double g = compositeElement(gammaToLinear(sg), gammaToLinear(dg), srcAlpha, destAlpha, outAlpha);
		double b = compositeElement(gammaToLinear(sb), gammaToLinear(db), srcAlpha, destAlpha, outAlpha);
		
		int ir = linearToGamma(r);
		int ig = linearToGamma(g);
		int ib = linearToGamma(b);
		int ia = (int) (outAlpha * 255.0);
		
		return ia << 24 | ir << 16 | ig << 8 | ib;
	}
	
	/** Converts one color sample from the srgb colorspace in the range [0 .. 255], into linear colorspace in the range [0.0 .. 1.0] */
	public static double gammaToLinear(int srgbElement) {
		return gammaToLinear(srgbElement, SRGB_GAMMA);
	}
	
	/** Converts one color sample from an srgb-like gamma-weighted colorspace in the range [0 .. 255], into linear colorspace in the range [0.0 .. 1.0] */
	public static double gammaToLinear(int gammaElement, double gamma) {
		if (gammaElement<0) return 0.0;
		
		double srgb = gammaElement / 255.0;
		if (srgb <= 0.04045) {
			return srgb / 12.92;
		} else if (srgb <= 1.0) {
			return Math.pow((srgb + 0.055) / 1.055, gamma);
		} else {
			return 1.0;
		}
	}
	
	/** Converts one color sample from linear colorspace in the range [0.0 .. 1.0] into the srgb colorspace in the range [0 .. 255] */
	public static int linearToGamma(double linearElement) {
		return linearToGamma(linearElement, SRGB_GAMMA);
	}
	
	/** Converts one color sample from linear colorspace in the range [0.0 .. 1.0] into an srgb-like gamma-weighted colorspace in the range [0 .. 255] */
	public static int linearToGamma(double linearElement, double gamma) {
		if (linearElement<0) {
			return 0;
		} else if (linearElement <= 0.0031308) {
			double gammaCorrected = linearElement * 12.92;
			return (int) (gammaCorrected * 255.0);
		} else if (linearElement <= 1.0) {
			double gammaCorrected = 1.055 * Math.pow(linearElement, 1.0 / gamma) - 0.055;
			return (int) (gammaCorrected * 255.0);
		} else {
			return 0xFF;
		}
	}
	
	/** Performs the standard Porter-Duff alpha blending operation for one set of linear color elements and their alphas */ //TODO: We're desperately going to need other blending operations
	public static double compositeElement(double src, double dest, double srcAlpha, double destAlpha, double outAlpha) {
		if (outAlpha<=0) return 0.0;
		double color = src*srcAlpha + dest*destAlpha*(1-srcAlpha);
		color /= outAlpha;
		
		return color;
	}
}
