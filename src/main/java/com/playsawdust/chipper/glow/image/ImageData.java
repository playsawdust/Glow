/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.image;

import java.util.Arrays;

public class ImageData {
	protected int width = 0;
	protected int height = 0;
	protected int[] data = new int[0];
	
	public ImageData() {}
	
	public ImageData(int width, int height) {
		this.width = width;
		this.height = height;
		this.data = new int[width*height];
	}
	
	public ImageData(int width, int height, int[] data) {
		this.width = width;
		this.height = height;
		this.data = data;
	}
	
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
	public int[] getData() { return this.data; }
	
	public void setPixel(int x, int y, int argb) {
		if (x<0 || x>=width || y<0 || y>=height) return;
		data[y*width + x] = argb;
	}
	
	public int getPixel(int x, int y) {
		if (x<0 || x>=width || y<0 || y>=height) return 0;
		return data[y*width + x];
	}
	
	public void clear() {
		Arrays.fill(data, 0);
	}
	
	public void clear(int clearColor) {
		Arrays.fill(data, clearColor);
	}
	
	public void resize(int width, int height) {
		int[] newData = new int[width*height];
		
		int copyWidth = Math.min(this.width, width);
		int copyHeight = Math.min(this.height, height);
		
		for(int y=0; y<copyHeight; y++) {
			if (y>=height) break;
			
			System.arraycopy(data, y*this.width, newData, y*width, copyWidth);
		}
		
		this.width = width;
		this.height = height;
		this.data = newData;
		
	}
	
	/** 
	 * Copies a row of pixels into this image, as if they were transferred using getPixel and setPixel, but faster.
	 *
	 * <h1><font size="20">UNTESTED! MAY SEGFAULT!</font></h1>
	 * <p>But when it's done, may drastically speed up non-BlendMode image operations.</b>
	 */
	protected void copyPixels(ImageData src, int srcX, int srcY, int dstX, int dstY, int len) {
		if (dstY<0 || dstY>=height) {
			//destination is off the image, so skip the copy altogether
			return;
		} else if (srcY<0 || srcY>=src.height) {
			//Take black for the whole copy
			
			if (dstX+src.width<=0 || dstX>=width) return; //the whole slice is off the destination page
			
			//Adjust destination params
			if (dstX<0) {
				//chop off stuff that's off the left edge
				len += dstX;
				dstX = 0;
			}
			
			if (dstX+len>=width) {
				//chop off stuff that's off the right edge
				len = width-dstX;
			}
			
			int ofs = dstY*width+dstX;
			Arrays.fill(data, ofs, ofs+len, 0);
		} else {
			
			if (dstX<0) {
				//chop off stuff that's off the left edge
				len += dstX;  //shorten len
				srcX -= dstX; //move srcX to the right
				dstX = 0;     //clamp dstX to zero
			}
			
			if (srcX<0) {
				//TODO: Take black from srcX..0, and then adjust params to the clipped image
			}
			
			// Shorten line starts by however much both lie off the edge of the image, reducing len in the process
			
			// Figure out how much to chop off and reduce len by due to dx being less than zero
			
			//TODO: Implement the actual System.arraycopy portion of this
		}
	}
}
