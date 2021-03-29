/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joml.Matrix3d;

import com.playsawdust.chipper.glow.image.BlendMode;
import com.playsawdust.chipper.glow.image.ImageData;
import com.playsawdust.chipper.glow.image.ImageEditor;
import com.playsawdust.chipper.glow.image.atlas.AbstractAtlas;
import com.playsawdust.chipper.glow.image.atlas.Atlas;
import com.playsawdust.chipper.glow.image.atlas.StitchedAtlas;
import com.playsawdust.chipper.glow.text.raster.RasterFont;
import com.playsawdust.chipper.glow.text.raster.RasterGlyph;
import com.playsawdust.chipper.glow.text.raster.RasterUtil;
import com.playsawdust.chipper.glow.util.MathUtil;
import com.playsawdust.chipper.glow.util.RectangleI;
import com.playsawdust.chipper.glow.util.VectorShape;

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
	
	/** Returns the scaling factor required to convert FontUnits into *pixels*.
	 * 
	 * @param pointSize The desired point size
	 * @param screenDPI The DPI of the screen to display on, available from {@link com.playsawdust.chipper.glow.Screen#getDPI()}
	 * @param contentScale The text dpi ratio, available from {@link com.playsawdust.chipper.glow.Screen#getContentScale()}. Or you can use 1.0 to ignore system settings.
	 * @return a number to multiply FontUnits values by to convert them to pixels
	 */
	public double getScalingFactor(double pointSize, double screenDPI, double contentScale) {
		double points = getScalingFactor(pointSize);
		double pixels = points * (screenDPI / 96.0); //TODO: Keep tweaking. Technically a point should be 1/72 inch, but that produces really wrong-looking text, so I'm eyeballing it at 1/96 since that's the base size these days
		return pixels * contentScale;
	}
	
	/**
	 * Returns true if the given code point has an associated glyph index
	 * @param codePoint the code point to search for
	 * @return true if codePoint has an associated glyph index
	 */
	public boolean isCodePointMapped(int codePoint) {
		return codePointToGlyph.containsKey(codePoint);
	}
	
	/** Returns the glyph number used by the supplied code point */
	public int getGlyphIndex(int codePoint) {
		Integer index = codePointToGlyph.get(codePoint);
		return (index==null) ? 0 : index;
	}
	
	/**
	 * Gets the number of glyphs in this Font
	 * @return the number of glyphs in the font, including the invalid character glyph and any unusued glyphs
	 */
	public int getGlyphCount() {
		return glyphs.size();
	}
	
	/**
	 * Gets a glyph by its *glyph* index. To get this index, call {@link #getGlyphIndex(int)}
	 * @param glyphIndex the index for the glyph to return
	 * @return the glyph at this index
	 */
	public VectorGlyph getGlyph(int glyphIndex) {
		return glyphs.get(glyphIndex);
	}
	
	/**
	 * Rasterizes this font into image tiles that can be used for fast rendering on common raster-based systems.
	 * @param pointSize The point size of the rasterized font (e.g. 12.0)
	 * @param dpi The dpi of the screen the font is expected to be rendered on. Use either 72.0 or 96.0 if you're not sure.
	 * @param contentScale A parameter usually set by the user in their system to scale text and UI elements. Use 1.0 if you don't have access to a setting like this.
	 * @param argbFill The fill color of the rasterized font, usually 0xFF_FFFFFF so that the text can be colored on the fly with a normal or multiply blend.
	 * @param argbOutline The outline color of the rasterized font, usually either 0xFF_000000 so that the outline contrasts with the fill color, or 0x00_000000 for an invisible outline.
	 * @param outlineWeight 1.0
	 * @param maxTileSize
	 * @param smoothness
	 * @return
	 */
	public RasterFont toRasterFont(double pointSize, double dpi, double contentScale, int argbFill, int argbOutline, double outlineWeight, int maxTileSize, double smoothness) {
		double scalingFactor = getScalingFactor(pointSize, dpi, contentScale);
		
		int glyphMaxHeight = (int) Math.ceil((maxY-minY) * scalingFactor) + (int) Math.ceil(outlineWeight);
		int outlineSS = (int) Math.ceil(outlineWeight*2*2);
		Matrix3d glyphTransform = new Matrix3d().scale(scalingFactor*2*2, -scalingFactor*2*2, 1);
		
		RasterFont font = new RasterFont();
		
		StitchedAtlas atlas = StitchedAtlas.usingShelf(glyphMaxHeight, 128, maxTileSize);
		ArrayList<StitchedAtlas> atlases = new ArrayList<>(); //Temp list so we can keep track here
		atlases.add(atlas);
		font.addPage(atlas);
		
		for(VectorGlyph glyph : glyphs) {
			VectorShape glyphShape = glyph.getShape();
			if (glyphShape.isEmpty()) {
				//Dummy glyph
				RasterGlyph dummy = new RasterGlyph();
				dummy.advanceWidth = 0;
				dummy.anchorX = 0;
				dummy.anchorY = 0;
				dummy.index = 0;
				dummy.page = 0;
				font.addGlyph(dummy);
				
				
				continue;
			}
			glyphShape.transform(glyphTransform);
			//glyphShape.gridFit();
			
			RectangleI glyphBox = glyphShape.getBoundingBox();
			int glyphWidth = glyphBox.width() + outlineSS + outlineSS+4;
			int glyphHeight = glyphBox.height() + outlineSS + outlineSS+4;
			int glyphLeft = -glyphBox.x();
			int glyphTop = -glyphBox.y();
			
			ImageData scratch = new ImageData(glyphWidth, glyphHeight);
			ImageEditor scratchEditor = ImageEditor.edit(scratch);
			
			if (outlineWeight>0.0 && (argbOutline >>> 24 != 0)) scratchEditor.outlineShape(glyphShape, glyphLeft+outlineSS, glyphTop+outlineSS, argbOutline, BlendMode.NORMAL, outlineWeight*2*2);
			scratchEditor.fillShape(glyphShape, glyphLeft+outlineSS, glyphTop+outlineSS, argbFill, BlendMode.NORMAL);
			
			ImageData sub1 = RasterUtil.supersample(scratch, smoothness);
			ImageData sub2 = RasterUtil.supersample(sub1, smoothness);
			
			boolean stitched = false; 
			for(int i=0; i<atlases.size(); i++) {
				atlas = atlases.get(i);
				int index = atlas.stitch(sub2);
				if (index!=-1) {
					stitched = true;
					
					RasterGlyph rglyph = new RasterGlyph();
					rglyph.advanceWidth = (int) Math.ceil(glyph.advanceWidth * scalingFactor);
					rglyph.anchorX = (int) Math.floor(glyphLeft/4.0);
					rglyph.anchorY = (int) Math.floor(glyphTop/4.0);
					
					
					rglyph.index = index;
					rglyph.page = i;
					font.addGlyph(rglyph);
					
					break;
				}
			}
			
			if (!stitched) {
				//Add a page
				atlas = StitchedAtlas.usingShelf(glyphMaxHeight, 128, maxTileSize);
				atlases.add(atlas);
				
				int index = atlas.stitch(sub2);
				if (index==-1) {
					//This glyph can't stitch, even given an entire page. So create a dummy glyph and Warn
					//TODO: Warn
					
					RasterGlyph dummy = new RasterGlyph();
					dummy.advanceWidth = 0;
					dummy.anchorX = 0;
					dummy.anchorY = 0;
					dummy.index = 0;
					dummy.page = 0;
					font.addGlyph(dummy);
				} else {
					RasterGlyph rglyph = new RasterGlyph();
					rglyph.advanceWidth = (int) Math.ceil(glyph.advanceWidth * scalingFactor);
					rglyph.anchorX = (int) Math.ceil(glyphLeft * scalingFactor);
					rglyph.anchorY = (int) Math.ceil(glyphTop * scalingFactor);
					rglyph.index = index;
					rglyph.page = atlases.size()-1;
					font.addGlyph(rglyph);
				}
			}
			
		}
		
		font.mapCharacters(codePointToGlyph);
		
		return font;
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
