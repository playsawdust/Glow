package com.playsawdust.chipper.glow.image;

import java.util.HashMap;

public class Font {
	private ClientImage image;
	
	private HashMap<Character, Glyph> glyphs = new HashMap<>();
	
	private HashMap<String, Glyph> ligatures = new HashMap<>();
	
	public static class Glyph {
		public int x;
	}
}
