/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.text.raster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.playsawdust.chipper.glow.image.BlendMode;
import com.playsawdust.chipper.glow.image.ImageEditor;
import com.playsawdust.chipper.glow.image.atlas.AbstractAtlas;
import com.playsawdust.chipper.glow.image.vector.RectangleI;

public class RasterFont {
	protected ArrayList<AbstractAtlas> pages = new ArrayList<>();
	protected ArrayList<RasterGlyph> glyphs = new ArrayList<>();
	protected HashMap<Integer, Integer> codePointToGlyph = new HashMap<>();
	
	public void addPage(AbstractAtlas page) {
		pages.add(page);
	}
	
	public void addGlyph(RasterGlyph glyph) {
		glyphs.add(glyph);
	}
	
	public int getPageCount() {
		return pages.size();
	}
	
	public AbstractAtlas getPage(int index) {
		return pages.get(index);
	}
	
	public RasterGlyph[] getGlyphs() {
		return glyphs.toArray(new RasterGlyph[glyphs.size()]);
	}
	
	public Map<Integer, Integer> getMappedCharacters() {
		HashMap<Integer, Integer> result = new HashMap<>();
		result.putAll(codePointToGlyph);
		return result;
	}
	
	public void mapCharacters(Map<Integer, Integer> glyphMap) {
		for(Map.Entry<Integer, Integer> entry : glyphMap.entrySet()) {
			codePointToGlyph.put(entry.getKey(), entry.getValue());
		}
		
	}
	
	public void drawString(ImageEditor editor, String s, int x, int y, BlendMode blendMode, double opacity) {
		int anchorX = x;
		int anchorY = y;
		
		for(int i=0; i<s.length(); i++) {
			char ch = s.charAt(i);
			if (ch==32) {
				anchorX += glyphs.get(0).advanceWidth;
				continue;
			}
			
			Integer glyphNum = codePointToGlyph.get((int)ch);
			if (glyphNum==null) glyphNum = 0;
			RasterGlyph glyph = glyphs.get(glyphNum);
			AbstractAtlas page = pages.get(glyph.page);
			RectangleI rect = page.getImageRegion(glyph.index);
			editor.drawImage(page, anchorX-glyph.anchorX, anchorY-glyph.anchorY, rect.x(), rect.y(), rect.width(), rect.height(), blendMode, opacity);
			
			anchorX += glyph.advanceWidth;
		}
	}
}
