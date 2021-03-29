/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.image;

/**
 * BlendMode represents a Porter-Duff compositing operation such as "overlay" or "burn".
 * These operations only apply to the color channels; the alpha channel is determined using the
 * standard {@code srcAlpha + destAlpha*(1-srcAlpha)} lerp.
 */
public interface BlendMode {
	public static final double SRGB_GAMMA = 2.4;
	
	public int blend(int src, int dest, double alpha);
	
	
	public static BlendMode NORMAL      = Simple.create((src, dest) -> src); //src*srcAlpha + dest*destAlpha*(1-srcAlpha);
	public static BlendMode MULTIPLY    = Simple.create((src, dest) -> src * dest);
	public static BlendMode DIVIDE      = Simple.create((src, dest) -> src / dest);
	public static BlendMode ADD         = Simple.create((src, dest) -> src + dest);
	public static BlendMode SUBTRACT    = Simple.create((src, dest) -> src - dest);
	
	public static BlendMode DODGE       = Simple.create((src, dest) -> dest / (1-src));
	public static BlendMode LINEAR_DODGE= ADD;
	public static BlendMode BURN        = Simple.create((src, dest) -> 1 - ((1 - dest) / src));
	public static BlendMode LINEAR_BURN = Simple.create((src, dest) -> src + dest - 1);
	
	public static BlendMode DARKEN      = Simple.create((src, dest) -> Math.min(src, dest));
	public static BlendMode LIGHTEN     = Simple.create((src, dest) -> Math.max(src, dest));
	public static BlendMode SCREEN      = Simple.create((src, dest) -> 1 - ((1-src) * (1-dest)));
	public static BlendMode OVERLAY     = Simple.create((src, dest) -> (src < 0.5) ? 2 * src * dest : 1 - ((1-src) * (1-dest)));
	
	public static BlendMode SOFT_LIGHT = Simple.create((src, dest) -> {
		if (dest < 0.5) {
			return src - (1 - 2*dest) * src * (1 - src);
		} else {
			return src + (2*dest - 1) * (gw3c(src) - src);
		}
	});
	
	public static interface Simple {
		public double blend(double src, double dest);
		
		public static BlendMode create(Simple s) {
			return (src, dest, alpha) -> {
				double sa = ((src >> 24) & 0xFF) / 255.0;
				double sr = gammaToLinear((src >> 16) & 0xFF);
				double sg = gammaToLinear((src >>  8) & 0xFF);
				double sb = gammaToLinear((src      ) & 0xFF);
				
				double da = ((dest >> 24) & 0xFF) / 255.0;
				double dr = gammaToLinear((dest >> 16) & 0xFF);
				double dg = gammaToLinear((dest >>  8) & 0xFF);
				double db = gammaToLinear((dest      ) & 0xFF);
				
				sa *= alpha;
				
				sr = clamp(s.blend(sr, dr));
				sg = clamp(s.blend(sg, dg));
				sb = clamp(s.blend(sb, db));
				
				double outAlpha = clamp(sa + da*(1-sa));
				double r = lerp(dr, sr, sa);
				double g = lerp(dg, sg, sa);
				double b = lerp(db, sb, sa);
				
				int ir = linearToGamma(r);
				int ig = linearToGamma(g);
				int ib = linearToGamma(b);
				int ia = (int) (outAlpha * 255.0);
				
				return ia << 24 | ir << 16 | ig << 8 | ib;
			};
		}
	}
	
	//public static int blend(int src, int dest, Simple mode) {
	//	return blend(src, dest, mode, 1.0);
	//}
	
	/**
	 * Blends two sRGB colors with alpha channels using the specified BlendMode
	 * @param src The color being drawn
	 * @param dest The color of the background 
	 * @param mode The BlendMode to use for the color channels
	 * @return The composited pixel
	 */
	/*
	public static int blend(int src, int dest, Simple mode, double alpha) {
		int sa = (src >> 24) & 0xFF;
		int sr = (src >> 16) & 0xFF;
		int sg = (src >>  8) & 0xFF;
		int sb = (src      ) & 0xFF;
		
		int da = (dest >> 24) & 0xFF;
		int dr = (dest >> 16) & 0xFF;
		int dg = (dest >>  8) & 0xFF;
		int db = (dest      ) & 0xFF;
		
		double srcAlpha = clamp( (sa / 255.0) * alpha);
		double destAlpha = clamp(da / 255.0);
		double outAlpha = clamp(srcAlpha + destAlpha*(1-srcAlpha));
		if (outAlpha<=0) return 0x00_000000;
		
		double r = clamp(mode.blend(gammaToLinear(sr), gammaToLinear(dr), srcAlpha, destAlpha, outAlpha)) / outAlpha;
		double g = clamp(mode.blend(gammaToLinear(sg), gammaToLinear(dg), srcAlpha, destAlpha, outAlpha)) / outAlpha;
		double b = clamp(mode.blend(gammaToLinear(sb), gammaToLinear(db), srcAlpha, destAlpha, outAlpha)) / outAlpha;
		
		int ir = linearToGamma(r);
		int ig = linearToGamma(g);
		int ib = linearToGamma(b);
		int ia = (int) (outAlpha * 255.0);
		
		return ia << 24 | ir << 16 | ig << 8 | ib;
	}*/
	
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
	
	public static double clamp(double value) {
		if (value<0.0) return 0.0;
		if (value>1.0) return 1.0;
		return value;
	}
	
	public static double clamp(double value, double min, double max) {
		if (value<min) return min;
		if (value>max) return max;
		return value;
	}
	
	public static double lerp(double a, double b, double t) {
		return a*(1-t) + b*t;
	}
	
	/**
	 * Applies gamma correction to the bottom layer of, for example, a soft-light blend, per w3c specs, in the same way Canvas and SVG do.
	 * Supposedly this corrects a discontinuity in the Photoshop function.
	 */
	private static double gw3c(double a) {
		if (a <= 0.25) {
			return ((16*a - 12) * a + 4) * a;
		} else {
			return Math.sqrt(a);
		}
	}
}
