package com.playsawdust.chipper.glow.text.raster;

public class RasterGlyph {
	/** Which glyph-page image contains this glyph */
	public int page;
	
	/** Which index does the glyph have on the glyph-page */
	public int index;
	
	/** The distance you need to move right from the origin of this glyph to the origin of the next glyph on the line */
	public int advanceWidth;
	
	/** X coordinate of the anchor (the origin of the glyph VectorShape, if this is a converted font) within the glyphArea rectangle */
	public int anchorX;
	/** Y coordinate of the anchor (the origin of the glyph VectorShape, if this is a converted font) within the glyphArea rectangle */
	public int anchorY;
}
