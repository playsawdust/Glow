package com.playsawdust.chipper.glow.text;

import com.playsawdust.chipper.glow.gl.Font;
import com.playsawdust.chipper.glow.gl.Texture;
import com.playsawdust.chipper.glow.image.ImageData;
import com.playsawdust.chipper.glow.util.RectangleI;

public class Glyph {
	protected String prefix;
	protected Texture texture;
	protected ImageData image;
	protected RectangleI tile;
	protected Font font;
	protected int id;
	
	public Font getFont() { return font; }
	public String getPrefix() { return prefix; }
	public Texture getTexture() { return texture; }
	public ImageData getImage() { return image; }
	public RectangleI getTile() { return tile; }
	
	public double getAdvanceWidth() {
		return tile.width()+font.getGlyphSpacing();
	}
	
	public double getAdvanceWidth(Glyph nextGlyph) {
		return tile.width()+font.getGlyphSpacing(this, nextGlyph);
	}
	
	public int getHeight() {
		return tile.height();
	}
}