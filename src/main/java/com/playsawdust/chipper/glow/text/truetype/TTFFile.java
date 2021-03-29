/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.text.truetype;

import com.google.common.collect.ClassToInstanceMap;
import com.playsawdust.chipper.glow.text.truetype.table.TTFCharacterMap;
import com.playsawdust.chipper.glow.text.truetype.table.TTFGlyph;
import com.playsawdust.chipper.glow.text.truetype.table.TTFGlyph.GlyphData;
import com.playsawdust.chipper.glow.text.truetype.table.TTFHead;
import com.playsawdust.chipper.glow.text.truetype.table.TTFHorizontalHeader;
import com.playsawdust.chipper.glow.text.truetype.table.TTFHorizontalMetrics;
import com.playsawdust.chipper.glow.text.truetype.table.TTFLocateGlyphs;
import com.playsawdust.chipper.glow.text.truetype.table.TTFTable;
import com.playsawdust.chipper.glow.util.Contour;
import com.playsawdust.chipper.glow.util.VectorShape;

public class TTFFile {
	private ClassToInstanceMap<TTFTable> tables;
	
	public TTFFile(ClassToInstanceMap<TTFTable> tables) {
		this.tables = tables;
	}
	
	
	public VectorShape getShape(int codePoint) {
		TTFCharacterMap characterMap = tables.getInstance(TTFCharacterMap.class);
		int glyphIndex = characterMap.glyphForCodePoint(codePoint);
		System.out.println("'"+(char)codePoint+"' -> "+glyphIndex);
		
		TTFLocateGlyphs locateGlyphs = tables.getInstance(TTFLocateGlyphs.class);
		int glyphLength = locateGlyphs.getGlyphLength(glyphIndex);
		System.out.println("Length: "+glyphLength);
		
		if (glyphLength!=0) {
			int glyphOffset = locateGlyphs.getGlyphOffset(glyphIndex);
			System.out.println("Offset: "+glyphOffset);
			
			TTFGlyph glyphData = tables.getInstance(TTFGlyph.class);
			GlyphData data = glyphData.readGlyph(glyphOffset);
			
			return data.getShape();
		} else {
			return new Contour().toShape();
		}
	}
	
	
	public double getPointScale(double pointSize) {
		TTFHead headTable = tables.getInstance(TTFHead.class);
		double unitsPerEm = headTable.unitsPerEm;
		
		double pixelsPerEm = pointSize;
		double emsPerUnit = 1.0 / unitsPerEm;
		return pixelsPerEm * emsPerUnit; //TODO: Make an additional correction for dpi, basically `result * (dpiSetting / dpiAssumedOrDetected)`
	}
	
	public double getAscent() {
		TTFHorizontalHeader hheaTable = tables.getInstance(TTFHorizontalHeader.class);
		return hheaTable.ascent;
	}
	
	public double getAscent(double pointSize) {
		return getAscent() * getPointScale(pointSize);
	}
	
	public double getDescent() {
		TTFHorizontalHeader hheaTable = tables.getInstance(TTFHorizontalHeader.class);
		return hheaTable.descent;
	}
	
	public double getDescent(double pointSize) {
		return getDescent() * getPointScale(pointSize);
	}
	
	public double getAdvanceWidth(int codePoint) {
		TTFCharacterMap cmapTable = tables.getInstance(TTFCharacterMap.class);
		int glyph = cmapTable.glyphForCodePoint(codePoint);
		
		TTFHorizontalMetrics hmtxTable = tables.getInstance(TTFHorizontalMetrics.class);
		if (glyph>=hmtxTable.metrics.size()) return 0;
		TTFHorizontalMetrics.GlyphMetrics metrics = hmtxTable.metrics.get(glyph);
		
		return metrics.getAdvanceWidth();
	}

	public double getLineSpacing() {
		TTFHorizontalHeader hheaTable = tables.getInstance(TTFHorizontalHeader.class);
		return hheaTable.lineGap;
	}
}
