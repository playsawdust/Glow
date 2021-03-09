package com.playsawdust.chipper.glow.text.raster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.playsawdust.chipper.glow.image.atlas.AbstractAtlas;

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
}
