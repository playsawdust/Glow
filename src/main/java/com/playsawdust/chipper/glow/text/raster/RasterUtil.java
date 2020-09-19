package com.playsawdust.chipper.glow.text.raster;

import com.playsawdust.chipper.glow.image.BlendMode;
import com.playsawdust.chipper.glow.image.ImageData;

public class RasterUtil {
	public static ImageData supersample(ImageData data, double smooth) {
		if (smooth<0.0) smooth = 0.0;
		if (smooth>1.0) smooth = 1.0;
		
		ImageData result = new ImageData(data.getWidth()/2, data.getHeight()/2);
		result.clear(0x00_000000);
		
		for(int y=0; y<result.getHeight(); y++) {
			for(int x=0; x<result.getWidth(); x++) {
				int a = data.getPixel(x*2, y*2);
				int b = data.getPixel(x*2+1, y*2);
				int c = data.getPixel(x*2, y*2+1);
				int d = data.getPixel(x*2+1, y*2+1);
				
				int aa = (a >> 24) & 0xFF;
				int ba = (b >> 24) & 0xFF;
				int ca = (c >> 24) & 0xFF;
				int da = (d >> 24) & 0xFF;
				
				int outA = (aa + ba + ca + da) / 4;
				//Lerp raw outA with a thresholded version of itself to create a sliding scale from smooth to hard edges
				int altA = (outA>112) ? 255 : 0;
				outA = (int)BlendMode.lerp(altA, outA, smooth);
				
				
				int ar = (a >> 16) & 0xFF;
				int br = (b >> 16) & 0xFF;
				int cr = (c >> 16) & 0xFF;
				int dr = (d >> 16) & 0xFF;
				
				int outR = (ar + br + cr + dr) / 4;
				
				int ag = (a >> 16) & 0xFF;
				int bg = (b >> 16) & 0xFF;
				int cg = (c >> 16) & 0xFF;
				int dg = (d >> 16) & 0xFF;
				
				int outG = (ag + bg + cg + dg) / 4;
				
				int ab = (a >> 16) & 0xFF;
				int bb = (b >> 16) & 0xFF;
				int cb = (c >> 16) & 0xFF;
				int db = (d >> 16) & 0xFF;
				
				int outB = (ab + bb + cb + db) / 4;
				
				int out = outA << 24 | outR << 16 | outG << 8 | outB;
				result.setPixel(x, y, out);
			}
		}
		
		
		return result;
	}
	
	/** will lay a 1px outline around the 0.5-alpha threshold. Best to use with opaque images! */
	public static ImageData outline(ImageData data, int argb) {
		ImageData result = new ImageData(data.getWidth(), data.getHeight());
		
		for(int y=0; y<result.getHeight(); y++) {
			for(int x=0; x<result.getWidth(); x++) {
				int pixel = data.getPixel(x, y);
				if (((pixel >> 24) & 0xFF) > 128) {
					result.setPixel(x, y, pixel);
					continue; //do nothing to opaque pixels
				}
				
				int xminus = data.getPixel(x-1,y) >>> 24;
				int xplus = data.getPixel(x+1, y) >>> 24;
				int yminus = data.getPixel(x, y-1) >>> 24;
				int yplus = data.getPixel(x, y+1) >>> 24;
				
				if (xminus>128 || xplus>128 || yminus>128 || yplus>128) {
					result.setPixel(x, y, argb);
				}
			}
		}
		
		return result;
	}
}
