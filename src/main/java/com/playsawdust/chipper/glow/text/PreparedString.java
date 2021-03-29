/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.text;

import java.util.ArrayList;
import java.util.List;

import com.playsawdust.chipper.glow.image.ImageData;

public final class PreparedString implements CharSequence {
	private final String text;
	private ArrayList<VectorGlyph> glyphs = new ArrayList<>();
	private int width;
	private int height;
	private boolean stillPreparing = false;
	
	private PreparedString(String text) {
		this.text = text;
		stillPreparing = true;
	}
	
	private PreparedString(String text, List<VectorGlyph> glyphs, int width, int height) {
		this.text = text;
		for(int i=0; i<glyphs.size(); i++) {
			this.glyphs.add(glyphs.get(i));
		}
		this.width = width;
		this.height = height;
		stillPreparing = false; //Assume all glyphs presented are loaded
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	@Override
	public char charAt(int index) {
		return text.charAt(index);
	}

	@Override
	public int length() {
		return text.length();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return text.subSequence(start, end);
	}
	
	/** Returns a ClientImage containing a rasterization of this String in the specified color. The non-text pixels in the image will be transparent for compositing. */
	/*
	public ClientImage rasterize(int argbColor) {
		ClientImage result = new ClientImage(width, height);
		int advance = 0;
		for(Font.Glyph glyph : glyphs) {
			//TODO: Rasterize glyphs
			
			advance += glyph.getAdvanceWidth();
		}
		
		return result;
	}*/
}
