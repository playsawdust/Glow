package com.playsawdust.chipper.glow.text;

import com.playsawdust.chipper.glow.util.VectorShape;

/**
 * A character image in a VectorFont.
 */
public class VectorGlyph {
	protected VectorShape shape;
	protected @FontUnits int advanceWidth;
	protected @FontUnits int leftSideBearing;
	
	public VectorGlyph(VectorShape shape, int advanceWidth, int leftSideBearing) {
		this.shape = shape;
		this.advanceWidth = advanceWidth;
		this.leftSideBearing = leftSideBearing;
	}
	
	/** Returns a deep copy of the VectorShape for this glyph. It is safe to transform the return value of this method. */
	public VectorShape getShape() {
		return shape.copy();
	}
	
	/** Returns the distance from the origin of this glyph to the typical origin of the next glyph. */
	public @FontUnits int getAdvanceWidth() {
		return advanceWidth;
	}
	
	/**
	 * Returns the left-side-bearing of this glyph. This is the distance from the origin to the left side of this glyph's
	 * bounding box.
	 */
	public @FontUnits int getLeftSideBearing() {
		return leftSideBearing;
	}
}