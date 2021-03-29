/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
