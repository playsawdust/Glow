package com.playsawdust.chipper.glow.text.raster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.playsawdust.chipper.glow.image.BlendMode;
import com.playsawdust.chipper.glow.image.ImageEditor;
import com.playsawdust.chipper.glow.image.atlas.AbstractAtlas;
import com.playsawdust.chipper.glow.util.RectangleI;

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
