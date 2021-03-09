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
	
	/**
	 * Creates a Raster version of this font, suitable for uploading as a paintable GPUResource.
	 * 
	 * <p>This is a slow process. It's recommended to either do this conversion off-thread, or as part of a loading screen where some kind of feedback is given to the user about why they're waiting.
	 * 
	 * @param pointSize The desired point size
	 * @param dpi The DPI of the screen to display on, available from {@link com.playsawdust.chipper.glow.Screen#getDPI()}
	 * @param contentScale The text dpi ratio, available from {@link com.playsawdust.chipper.glow.Screen#getContentScale()}. Or you can use 1.0 to ignore system settings.
	 * @return A rasterized version of this font.
	 */
	public ImageData toRasterFont_(double pointSize, double dpi, double contentScale, int argbFill, int argbOutline, double outlineWeight) {
		double scalingFactor = getScalingFactor(pointSize, dpi, contentScale);
		
		int lineHeight = (int) Math.ceil((maxY-minY) * scalingFactor);
		
		//The biggest problem here is laying out all the glyphs onto a page.
		ArrayList<ArrayList<VectorGlyph>> shelves = new ArrayList<>();
		ArrayList<ArrayList<Integer>> indexShelves = new ArrayList<>(); //It's 'indices', I know.
		ArrayList<Integer> shelfWidths = new ArrayList<>();
		for(int glyphIndex=0; glyphIndex<glyphs.size(); glyphIndex++) {
			VectorGlyph glyph = glyphs.get(glyphIndex);
		//for(VectorGlyph glyph : glyphs) {
			RectangleI glyphBox = glyph.getShape().getBoundingBox();
			int glyphWidth = (int) Math.ceil((glyphBox.width() + (int)Math.ceil(outlineWeight)) * scalingFactor);
			
			boolean laidOut = false;
			int atlasHeight = shelves.size() * (lineHeight + (int) Math.ceil(outlineWeight)); //The current atlas height is the width limit for each glyph row. Try to keep it square.
			for(int i=0; i<shelves.size(); i++) {
				ArrayList<VectorGlyph> shelf = shelves.get(i);
				ArrayList<Integer> indexShelf = indexShelves.get(i);
				int shelfWidth = shelfWidths.get(i);
			//for(ArrayList<VectorGlyph> shelf : shelves) {
				/*int shelfWidth = 0;
				for(VectorGlyph shelvedGlyph : shelf) {
					RectangleI box = shelvedGlyph.getShape().getBoundingBox();
					shelfWidth += box.width() + Math.ceil(outlineWeight);
				}*/
				
				if (shelfWidth+glyphWidth <= atlasHeight) {
					shelf.add(glyph);
					indexShelf.add(glyphIndex);
					int existingSize = shelfWidths.get(i);
					shelfWidths.set(i, existingSize+glyphWidth);
					laidOut = true;
					break;
				}
			}
			
			if (!laidOut) {
				ArrayList<VectorGlyph> newShelf = new ArrayList<>();
				newShelf.add(glyph);
				shelves.add(newShelf);
				shelfWidths.add(glyphWidth);
				ArrayList<Integer> newIndexShelf = new ArrayList<>();
				newIndexShelf.add(glyphIndex);
				indexShelves.add(newIndexShelf);
			}
		}
		
		//Figure out the dimensions of the atlas
		//int atlasWidth = 0;
		int atlasHeight = shelves.size() * (lineHeight + (int) Math.ceil(outlineWeight));
		/*
		for(ArrayList<VectorGlyph> shelf : shelves) {
			int shelfWidth = 0;
			for(VectorGlyph shelvedGlyph : shelf) {
				RectangleI box = shelvedGlyph.getShape().getBoundingBox();
				shelfWidth += box.width() + Math.ceil(outlineWeight);
			}
			atlasWidth = Math.max(atlasWidth, shelfWidth);
		}
		System.out.println("Atlas will be "+atlasWidth+"x"+atlasHeight);*/
		
		atlasHeight = MathUtil.nextPowerOf2(atlasHeight);
		ImageData atlas = new ImageData(atlasHeight, atlasHeight);
		
		RasterFont result = new RasterFont();
		
		ImageEditor atlasEditor = ImageEditor.edit(atlas);
		
		int glyphMaxWidth = (int) Math.ceil((maxX-minX) * scalingFactor) + (int) Math.ceil(outlineWeight);
		int glyphMaxHeight = (int) Math.ceil((maxY-minY) * scalingFactor) + (int) Math.ceil(outlineWeight);
		ImageData scratch = new ImageData(glyphMaxWidth * 2 * 2, glyphMaxHeight * 2 * 2);
		ImageEditor scratchEditor = ImageEditor.edit(scratch);
		int outlineSS = (int) Math.ceil(outlineWeight*2*2);
		
		Matrix3d glyphTransform = new Matrix3d().scale(scalingFactor*2*2, -scalingFactor*2*2, 1);
		
		
		for(int shelfIndex = 0; shelfIndex < shelves.size(); shelfIndex++) {
			int atlasY = shelfIndex * lineHeight;
			int atlasX = 0;
			ArrayList<VectorGlyph> shelf = shelves.get(shelfIndex);
			ArrayList<Integer> indexShelf = indexShelves.get(shelfIndex);
			for(int glyphIndex = 0; glyphIndex < shelf.size(); glyphIndex++) {
				VectorGlyph glyph = shelf.get(glyphIndex);
				VectorShape glyphShape = glyph.getShape();
				if (glyphShape.isEmpty()) continue;
				glyphShape.transform(glyphTransform);
				
				RectangleI glyphBox = glyphShape.getBoundingBox();
				int glyphWidth = glyphBox.width() + (int)Math.ceil(outlineWeight);
				int glyphLeft = -glyphBox.x(); //TODO: Pour this value into the atlas description!
				int glyphTop = -glyphBox.y(); //TODO: " "
				
				scratch.clear(0x00_000000);
				
				
				//int cursorGlyph = getGlyphIndex('C');
				//int cursorWidth = (int) (emSize * scalingFactor);
				//boolean isCursor = indexShelf.get(glyphIndex)==cursorGlyph;
				//if (isCursor) {
				//	scratchEditor.fillRect(0, 0, cursorWidth, glyphMaxHeight, 0xFF_FF00FF, BlendMode.NORMAL);
					
				//	System.out.println("Cursor glyph should layout at "+glyphLeft+", "+glyphTop);
				//}
				
				
				
				if (outlineWeight>=0 && (argbOutline >>> 24 != 0)) scratchEditor.outlineShape(glyphShape, glyphLeft+outlineSS, glyphTop+outlineSS, argbOutline, BlendMode.NORMAL, outlineWeight*2*2);
				scratchEditor.fillShape(glyphShape, glyphLeft+outlineSS, glyphTop+outlineSS, argbFill, BlendMode.NORMAL);
				
				ImageData sub1 = RasterUtil.supersample(scratch, 0.5);
				ImageData sub2 = RasterUtil.supersample(sub1, 1.0);
				
				atlasEditor.drawImage(sub2, atlasX, atlasY, BlendMode.NORMAL);
				
				if (indexShelf.get(glyphIndex)<255) System.out.println("Painted Glyph: "+indexShelf.get(glyphIndex)+", x: "+atlasX+", y: "+atlasY+", FUW: "+glyphBox.width()+", FUH: "+glyphBox.height());
				
				atlasX += (glyphWidth/2/2) + (outlineSS/2/2);
			}
			
			
		}
		
		return atlas;
	}
	
	public RasterFont toRasterFont(double pointSize, double dpi, double contentScale, int argbFill, int argbOutline, double outlineWeight, int maxTileSize) {
		double scalingFactor = getScalingFactor(pointSize, dpi, contentScale);
		
		//int lineHeight = (int) Math.ceil((maxY-minY) * scalingFactor);
		//int glyphMaxWidth = (int) Math.ceil((maxX-minX) * scalingFactor) + (int) Math.ceil(outlineWeight);
		int glyphMaxHeight = (int) Math.ceil((maxY-minY) * scalingFactor) + (int) Math.ceil(outlineWeight);
		int outlineSS = (int) Math.ceil(outlineWeight*2*2);
		Matrix3d glyphTransform = new Matrix3d().scale(scalingFactor*2*2, -scalingFactor*2*2, 1);
		
		RasterFont font = new RasterFont();
		
		StitchedAtlas atlas = StitchedAtlas.usingShelf(glyphMaxHeight, 128, maxTileSize);
		ArrayList<StitchedAtlas> atlases = new ArrayList<>(); //Temp list so we can keep track here
		atlases.add(atlas);
		font.addPage(atlas);
		//ImageData scratch = new ImageData(glyphMaxWidth * 2 * 2, glyphMaxHeight * 2 * 2);
		//ImageEditor scratchEditor = ImageEditor.edit(scratch);
		
		//System.out.println("GlyphMaxHeight: "+glyphMaxHeight);
		
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
			
			RectangleI glyphBox = glyphShape.getBoundingBox();
			//System.out.println(glyphBox);
			int glyphWidth = glyphBox.width() + outlineSS + outlineSS;
			int glyphHeight = glyphBox.height() + outlineSS + outlineSS;
			int glyphLeft = -glyphBox.x();
			int glyphTop = -glyphBox.y();
			
			ImageData scratch = new ImageData(glyphWidth, glyphHeight); //glyphMaxHeight*2*2);
			ImageEditor scratchEditor = ImageEditor.edit(scratch);
			
			//scratch.clear(0x00_000000);
			
			if (outlineWeight>=0 && (argbOutline >>> 24 != 0)) scratchEditor.outlineShape(glyphShape, glyphLeft+outlineSS, glyphTop+outlineSS, argbOutline, BlendMode.NORMAL, outlineWeight*2*2);
			scratchEditor.fillShape(glyphShape, glyphLeft+outlineSS, glyphTop+outlineSS, argbFill, BlendMode.NORMAL);
			
			ImageData sub1 = RasterUtil.supersample(scratch, 0.5);
			ImageData sub2 = RasterUtil.supersample(sub1, 1.0);
			
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
