/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.text.truetype.table;

import java.io.IOException;
import java.util.ArrayList;

import com.google.common.collect.ClassToInstanceMap;
import com.playsawdust.chipper.glow.text.truetype.TTFDataInput;

import blue.endless.jankson.JsonObject;

public class TTFLocateGlyphs extends TTFTable {
	public static final String TAG_NAME = "loca";
	public static final int TAG = tagNameToInt(TAG_NAME);
	
	public static ArrayList<Integer> glyphOffsets = new ArrayList<>();
	public static ArrayList<Integer> glyphLengths = new ArrayList<>();
	
	public TTFLocateGlyphs(int offset, int length) { super(offset, length); }
	
	public int getGlyphOffset(int glyphIndex) {
		if (glyphIndex<0 || glyphIndex>=glyphOffsets.size()) return glyphOffsets.get(glyphOffsets.size()-1);
		return glyphOffsets.get(glyphIndex);
	}
	
	public int getGlyphLength(int glyphIndex) {
		if (glyphIndex<0 || glyphIndex>=glyphLengths.size()) return glyphLengths.get(glyphLengths.size()-1);
		return glyphLengths.get(glyphIndex);
	}
	
	@Override
	public void load(TTFDataInput data, ClassToInstanceMap<TTFTable> tables) throws IOException {
		TTFHead headTable = tables.getInstance(TTFHead.class);
		if (headTable==null) throw new IOException("Missing required table 'head'");
		
		int maxGlyphs = 0;
		TTFMaximumProfile maxpTable = tables.getInstance(TTFMaximumProfile.class);
		if (maxpTable==null) {
			maxGlyphs = getLength() / ((headTable.indexToLocFormat==0) ? 2 : 4);
		} else {
			maxGlyphs = maxpTable.numGlyphs+1; //There'll be one extra missingno glyph
		}
		
		if (headTable.indexToLocFormat==0) {
			//"short" uint16 offsets that need to be multiplied by 2 and added to the offset of the 'glyf' table
			int lastOffset = 0;
			for(int i=0; i<maxGlyphs; i++) {
				int offset = data.readUInt16() * 2;
				if (!glyphOffsets.isEmpty()) glyphLengths.add(offset - lastOffset);
				glyphOffsets.add(offset);
				lastOffset = offset;
			}
			glyphLengths.add(1);//TODO: Figure out last glyph's length?
			
		} else if (headTable.indexToLocFormat==1) {
			//"long" uint32 offsets that can be used directly relative to the start of the 'glyf' table
			int lastOffset = 0;
			for(int i=0; i<maxGlyphs; i++) {
				int offset = data.readUInt32Clamped();
				if (!glyphOffsets.isEmpty()) glyphLengths.add(offset - lastOffset);
				glyphOffsets.add(offset);
				lastOffset = offset;
			}
			glyphLengths.add(1); //TODO: Figure out last glyph's length?
		}
	}
	
	@Override
	public JsonObject toJson() {
		JsonObject result = new JsonObject();
		
		result.put("offsets", json("("+glyphOffsets.size()+" glyph offsets)"));
		
		return result;
	}
}
