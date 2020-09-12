package com.playsawdust.chipper.glow.gl;

import java.util.ArrayList;
import java.util.HashMap;

import com.playsawdust.chipper.glow.image.AtlasImage;
import com.playsawdust.chipper.glow.image.ClientImage;
import com.playsawdust.chipper.glow.text.Glyph;

public class Font {
	protected Font fallback;
	/**
	 * Holds CodePages which don't really stand for a particular spot in the unicode table, such as a "th" ligature,
	 * or the "rainbow flag" emoji which consists of FLAG+VS16+ZWJ+RAINBOW. Can also hold translations from shortcodes
	 * like :thonking:.
	 */
	protected ArrayList<CodePage> extendedGraphemePages = new ArrayList<>();
	/**
	 * Holds the translation table from Strings to glyphs. **This must be populated ahead of time**, as it is otherwise
	 * impossible to predict what will transform into a special glyph.
	 */
	protected HashMap<String, Glyph> extendedGraphemes = new HashMap<>();
	
	protected HashMap<Integer, CodePage> codePages = new HashMap<>();
	protected HashMap<Integer, Glyph> glyphs = new HashMap<>();
	private double spacing = 1;
	
	public Glyph getFirstGlyph(String s) {
		for(String graphemeTrigger : extendedGraphemes.keySet()) {
			if (s.startsWith(graphemeTrigger)) {
				return extendedGraphemes.get(s);
			}
		}
		
		int firstCodePoint = s.codePointAt(0);
		for(Integer glyphTrigger : glyphs.keySet()) {
			if (glyphTrigger == firstCodePoint) return glyphs.get(glyphTrigger);
		}
		
		return null; //TODO: ALWAYS return a Glyph, even if it's a missingno glyph!
	}
	
	public double getGlyphSpacing() {
		return spacing;
	}
	
	
	
	
	
	private class CodePage {
		private AtlasImage atlas;
		
		public ClientImage getImage() {
			return atlas.getImage();
		}
	}
}
