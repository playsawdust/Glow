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
	
	public TTFLocateGlyphs(int offset, int length) { super(offset, length); }
	
	public int getGlyphOffset(int glyphIndex) {
		if (glyphIndex<0 || glyphIndex>=glyphOffsets.size()) return glyphOffsets.get(glyphOffsets.size()-1);
		return glyphOffsets.get(glyphIndex);
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
			for(int i=0; i<maxGlyphs; i++) {
				int offset = data.readUInt16() * 2;
				glyphOffsets.add(offset);
			}
			
		} else if (headTable.indexToLocFormat==1) {
			//"long" uint32 offsets that can be used directly relative to the start of the 'glyf' table
			for(int i=0; i<maxGlyphs; i++) {
				int offset = data.readUInt32Clamped();
				glyphOffsets.add(offset);
			}
		}
	}
	
	@Override
	public JsonObject toJson() {
		JsonObject result = new JsonObject();
		
		result.put("offsets", json("("+glyphOffsets.size()+" glyph offsets)"));
		
		return result;
	}
}
