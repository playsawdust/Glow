/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.gl;

import java.util.Map;

import com.playsawdust.chipper.glow.image.atlas.AbstractAtlas;
import com.playsawdust.chipper.glow.image.vector.RectangleI;
import com.playsawdust.chipper.glow.text.raster.RasterFont;
import com.playsawdust.chipper.glow.text.raster.RasterGlyph;

public class BakedFont implements GPUResource {
	protected RegionPage[] regions;
	protected RasterGlyph[] glyphs;
	private Texture[] textures;
	protected Map<Integer, Integer> codePointToGlyph;
	
	private BakedFont(RasterFont unbaked) {
		glyphs = unbaked.getGlyphs();
		codePointToGlyph = unbaked.getMappedCharacters();
		
		textures = new Texture[unbaked.getPageCount()];
		regions = new RegionPage[unbaked.getPageCount()];
		for(int i=0; i<textures.length; i++) {
			AbstractAtlas atlas = unbaked.getPage(i);
			textures[i] = Texture.of(atlas);
			regions[i] = new RegionPage(atlas.getImageRegions());
		}
	}
	
	public void paintString(Painter painter, CharSequence s, int x, int y, int color) {
		int anchorX = x;
		int anchorY = y;
		
		//painter.paintTexture(textures[0], 0, 0);
		
		
		for(int i=0; i<s.length(); i++) {
			char ch = s.charAt(i);
			if (ch==32) {
				anchorX += glyphs[0].advanceWidth;
				continue;
			}
			
			Integer glyphNum = codePointToGlyph.get((int)ch);
			if (glyphNum==null) glyphNum = 0;
			RasterGlyph glyph = glyphs[glyphNum];
			RegionPage page = regions[glyph.page];
			RectangleI rect = page.getRegion(glyph.index);
			//painter.paint
			//painter.paintTexture(textures[glyph.page], rect, anchorX-glyph.anchorX, anchorY-glyph.anchorY, color);
			painter.paintTexture(textures[glyph.page], anchorX-glyph.anchorX, anchorY-glyph.anchorY, rect.width(), rect.height(), rect.x(), rect.y(), rect.width(), rect.height(), color);
			
			anchorX += glyph.advanceWidth;
		}
	}
	
	//implements GPUResource {
		@Override
		public void free() {
			//proactively unlink and drop objects from the heap too
			for(int i=0; i<textures.length; i++) {
				textures[i].free();
				textures[i] = null;
			}
			textures = null;
			regions = null;
			glyphs = null;
		}
	//}
		
	private static class RegionPage {
		RectangleI[] regions;
		
		public RegionPage(RectangleI[] rects) {
			this.regions = rects;
		}
		
		public RectangleI getRegion(int index) {
			return regions[index % regions.length];
		}
	}
	
	public static BakedFont of(RasterFont unbaked) {
		return new BakedFont(unbaked);
	}
}
