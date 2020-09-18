package com.playsawdust.chipper.glow.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import blue.endless.jankson.JsonObject;

public class VectorFont {
	
	protected String typefaceName;
	protected String variantName;
	
	protected @FontUnits int ascent;
	protected @FontUnits int descent;
	protected @FontUnits int emSize;
	protected @FontUnits int maxAdvanceWidth;
	protected @FontUnits int lineSpacing;
	
	protected @FontUnits int maxX;
	protected @FontUnits int maxY;
	protected @FontUnits int minX;
	protected @FontUnits int minY;
	
	protected ArrayList<VectorGlyph> glyphs = new ArrayList<>();
	protected HashMap<Integer, Integer> codePointToGlyph = new HashMap<>(); //TODO: This is probably enormous compared to what it stores, we need to investigate whether HPPC or fastutil would be appropriate here, like a ScatterMap
	
	protected JsonObject metadata = new JsonObject();
	
	/**
	 * Returns a String naming this Font, such as "Andale" or "Courier", shared between all style variants of its typeface.
	 */
	public String getTypefaceName() {
		return typefaceName; 
	}
	
	/**
	 * Returns the name of this Font's variant, such as "book", "italic", "condensed", or "sans thin".
	 */
	public String getVariantName() {
		return variantName;
	}
	
	/** Returns non-semantic information about this font, such as name of the foundry which created it. */
	public JsonObject getMetadata() {
		return metadata.clone(); //Defensive copy just to be safe
	}
	
	/**
	 * Returns the maximum height of features of lowercase letters which rise above the mean-line of the font, such as the tall legs of 'b', 'h', and 'd'.
	 * This is often higher than the "capital height", surmounted only by some diacritics.
	 */
	public @FontUnits int getAscent() {
		return ascent;
	}
	
	/** Returns the maximum depth of features that descend below the baseline. */
	public @FontUnits int getDescent() {
		return descent;
	}
	
	/** Get the leftmost point of any glyph in this font. This can be used for determining boundaries for rasterization. */
	public @FontUnits int getMinimumX() {
		return minX;
	}
	
	/**
	 * Returns the total, lowest coordinate attainable below the baseline of this font. This can be used for determining boundaries for rasterization.
	 * 
	 * <p>Note: This number will usually be negative.
	 */
	public @FontUnits int getMinimumY() {
		return minY;
	}
	
	/**
	 * Get the rightmost point of any glyph in this font. This can be used for determining boundaries for rasterization.
	 * 
	 * <p>Note: This can often be extremely wide depending on the font, due to inclusion of glyphs like triple-ems, or the cuneiform
	 * "lugal opposing lugal" (U+12219), or the infamous (also-cuneiform) nine shar 2 (U+1242B). When rasterizing, to save space, it's often wise to
	 * check the individual glyph extents to see if you can pack individual glyph pages tighter than the font's overall maximum.
	 */
	public @FontUnits int getMaximumX() {
		return maxX;
	}
	
	/**
	 * Returns the total, maximum height attainable above the baseline in this font. This can be used for determining boundaries for rasterization.
	 */
	public @FontUnits int getMaximumY() {
		return maxY;
	}
	
	/**
	 * Returns the Em size, in font units.
	 *
	 * <p>The value of this is 1000 in some older PostScript fonts, but for TTF, OTF, and WOFF fonts this should, for
	 * best compatibility, be 2048.
	 */
	public @FontUnits int getEmSize() {
		return emSize;
	}
	
	/**
	 * Returns the maximum horizontal distance from the origin of one glyph to the origin of the following glyph.
	 */
	public @FontUnits int getMaximumAdvanceWidth() {
		return maxAdvanceWidth;
	}
	
	/**
	 * Returns the extra distance which needs to be inserted between consecutive lines of text in this font. This value should typically be zero.
	 */
	public @FontUnits int getLineSpacing() {
		return lineSpacing;
	}
	
	/** Returns the scaling factor required to convert FontUnits into points. */
	public double getScalingFactor(double pointSize) {
		return pointSize / (double)emSize;
	}
	
	public boolean isCodePointMapped(int codePoint) {
		return codePointToGlyph.containsKey(codePoint);
	}
	
	/** Returns the glyph number used by the supplied code point */
	public int getGlyphIndex(int codePoint) {
		Integer index = codePointToGlyph.get(codePoint);
		return (index==null) ? 0 : index;
	}
	
	public int getGlyphCount() {
		return glyphs.size();
	}
	
	public VectorGlyph getGlyph(int glyphIndex) {
		return glyphs.get(glyphIndex);
	}
	
	// ###                                                                                         ### //
	// ### Mutators - only use these if you really know what you're doing, and you're font editing ### //
	// ###                                                                                         ### //
	
	public void setTypefaceName(String name) { this.typefaceName = name; }
	public void setVariantName(String name) { this.variantName = name; }
	public void setMetadata(JsonObject meta) { this.metadata = meta; }
	
	public void setAscent(@FontUnits int ascent) { this.ascent = ascent; }
	public void setDescent(@FontUnits int descent) { this.descent = descent; }
	public void setEmSize(@FontUnits int emSize) { this.emSize = emSize; }
	public void setMaxAdvanceWidth(@FontUnits int width) { this.maxAdvanceWidth = width; }
	public void setLineSpacing(@FontUnits int spacing) { this.lineSpacing = spacing; }
	
	public void setLimits(int xMin, int yMin, int xMax, int yMax) {
		this.minX = xMin;
		this.minY = yMin;
		this.maxX = xMax;
		this.maxY = yMax;
	}
	
	public int addGlyph(VectorGlyph glyph) {
		glyphs.add(glyph);
		return glyphs.size()-1;
	}
	
	public void removeGlyph(VectorGlyph glyph) {
		glyphs.remove(glyph);
	}
	
	public void clearGlyphs() {
		glyphs.clear();
	}
	
	public void setGlyphs(List<VectorGlyph> glyphs) {
		this.glyphs.clear();
		for(VectorGlyph glyph : glyphs) {
			this.glyphs.add(glyph);
		}
	}
	
	public void setGlyphForCodePoint(int codePoint, int glyphId) {
		codePointToGlyph.put(codePoint, glyphId);
	}
}
