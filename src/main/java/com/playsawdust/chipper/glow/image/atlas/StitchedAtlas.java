/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.image.atlas;

import com.google.common.base.Preconditions;
import com.playsawdust.chipper.glow.image.ImageData;
import com.playsawdust.chipper.glow.image.ShelfStitcher;
import com.playsawdust.chipper.glow.image.Stitcher;
import com.playsawdust.chipper.glow.util.MathUtil;
import com.playsawdust.chipper.glow.util.RectangleI;

public class StitchedAtlas extends AbstractAtlas {
	protected Stitcher stitcher;
	
	private StitchedAtlas() {}
	
	/** Stitches a new subimage into this Atlas. Returns the index of the new Image */
	public int stitch(ImageData subimage) {
		RectangleI layout = new RectangleI(0, 0, subimage.getWidth(), subimage.getHeight());
		boolean stitched = stitcher.stitch(layout);
		if (!stitched) {
			return -1;
		}
		
		//Resize this image if the rectangle hangs off the right or bottom edge
		if (layout.getRight()>=width || layout.getBottom()>=height) {
			int cur = Math.max(width, height);
			int curPo2 = MathUtil.nextPowerOf2(cur);
			if (cur!=curPo2 && layout.getRight()<curPo2 && layout.getBottom()<curPo2) {
				//Somehow we weren't a power of 2, but resizing to a power of 2 makes it fit.
				this.resize(curPo2, curPo2);
			} else {
				int next = curPo2*2; //next power of 2
				while(layout.getRight()>=next || layout.getBottom()>=next) next *= 2; //however many more powers we need
				
				this.resize(next, next);
			}
		}
		
		this.images.add(layout);
		int resultIndex = this.images.size()-1;
		this.replaceImage(resultIndex, subimage); //copy the image data in
		
		return resultIndex;
	}
	
	
	/** Creates a StitchedAtlas pre-loaded with a shelf stitcher which can add subimages into the image */
	public static StitchedAtlas usingShelf(int shelfHeight, int initialSize, int maxSize) {
		Preconditions.checkArgument(initialSize<=maxSize, "Initial size cannot be bigger than maxSize");
		
		StitchedAtlas result = new StitchedAtlas();
		result.width = initialSize;
		result.height = initialSize;
		result.data = new int[initialSize * initialSize];
		result.stitcher = new ShelfStitcher(shelfHeight, maxSize);
		
		return result;
	}
	
	public static StitchedAtlas using(Stitcher stitcher, int initialSize) {
		StitchedAtlas result = new StitchedAtlas();
		
		result.width = initialSize;
		result.height = initialSize;
		result.data = new int[initialSize * initialSize];
		result.stitcher = stitcher;
		
		return result;
	}
}
