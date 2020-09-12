package com.playsawdust.chipper.glow.text;

import com.playsawdust.chipper.glow.gl.Font;
import com.playsawdust.chipper.glow.gl.Texture;
import com.playsawdust.chipper.glow.image.AtlasImage;
import com.playsawdust.chipper.glow.image.ImageData;

public class Glyph {
	protected String prefix;
	protected Texture texture;
	protected ImageData image;
	protected AtlasImage.Tile tile;
	protected Font font;
	
	public Font getFont() { return font; }
	public String getPrefix() { return prefix; }
	public Texture getTexture() { return texture; }
	public ImageData getImage() { return image; }
	public AtlasImage.Tile getTile() { return tile; }
	
	public double getAdvanceWidth() {
		return tile.getWidth()+font.getGlyphSpacing();
	}
}