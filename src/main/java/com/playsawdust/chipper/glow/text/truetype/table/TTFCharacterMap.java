package com.playsawdust.chipper.glow.text.truetype.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.ClassToInstanceMap;
import com.playsawdust.chipper.glow.text.truetype.TTFDataInput;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonObject;

public class TTFCharacterMap extends TTFTable {
	public static final String TAG_NAME = "cmap";
	public static final int TAG = tagNameToInt(TAG_NAME);
	
	int version;
	ArrayList<CMapSubtable> encodings = new ArrayList<>();
	HashMap<Integer, Integer> characterToGlyph = new HashMap<>();
	
	public TTFCharacterMap(int offset, int length) { super(offset, length); }
	
	@Override
	public void load(TTFDataInput data, ClassToInstanceMap<TTFTable> tables) throws IOException {
		
		int tableStart = data.position();
		
		int version = data.readUInt16();
		if (version!=0) throw new IOException("Can't read character-glyph map version "+version);
		int numEncodings = data.readUInt16();
		
		//ArrayList<CMapSubtable> subtables = new ArrayList<>();
		for(int i=0; i<numEncodings; i++) {
			CMapSubtable subtable = new CMapSubtable();
			subtable.platformId = data.readUInt16();
			subtable.platformEncodingId = data.readUInt16();
			subtable.offset = data.readUInt32Clamped();
			encodings.add(subtable);
		}
		
		CMapSubtable preferredSubtable = null;
		for(CMapSubtable subtable : encodings) {
			TTFPlatform.Id platform = TTFPlatform.Id.of(subtable.platformId);
			
			if (platform==TTFPlatform.Id.UNICODE) {
				preferredSubtable = subtable; //Always prefer pure-unicode
			} else if (platform==TTFPlatform.Id.MICROSOFT) {
				if (preferredSubtable==null) preferredSubtable = subtable; //Only use Microsoft if there's no pure unicode.
				continue;
			}
		}
		
		
		CMapSubtable subtable = preferredSubtable;
		//System.out.println("Selecting encoding "+TTFPlatform.getPlatformString(subtable.platformId, subtable.platformEncodingId));
		data.seek(tableStart + subtable.offset);
		
		int format = data.readUInt16();
		if (format==4) {
			int length = data.readUInt16(); // == 262
			int language = data.readUInt16(); //SHOULD BE ZERO - this is legacy QuickDraw stuff even Apple discourages using!
			int segCount = data.readUInt16() / 2;
			int lsearchRange = data.readUInt16(); //Used for fast binary searches of the table, which doesn't even need to exist on the disk
			int lentrySelector = data.readUInt16();
			int lrangeShift = data.readUInt16();
			int[] endCodes = new int[segCount];
			for(int i=0; i<segCount; i++) {
				endCodes[i] = data.readUInt16();
			}
			data.readUInt16(); //reserved / padding
			int[] startCodes = new int[segCount];
			for(int i=0; i<segCount; i++) {
				startCodes[i] = data.readUInt16();
			}
			int[] idDeltas = new int[segCount];
			for(int i=0; i<segCount; i++) {
				idDeltas[i] = data.readUInt16();
			}
			int idRangeOffsetPointer = data.position();
			int[] idRangeOffsets = new int[segCount];
			for(int i=0; i<segCount; i++) {
				idRangeOffsets[i] = data.readUInt16();
			}
			
			int glyphCount = 0;
			for(int i=0; i<segCount; i++) {
				int rangeSize = endCodes[i] - startCodes[i] + 1;
				glyphCount += rangeSize;
				if (endCodes[i]<startCodes[i]) continue; //Broken range
				for(int j=startCodes[i]; j<=endCodes[i]; j++) {
					if (idRangeOffsets[i]==0) {
						characterToGlyph.put(j, j+idDeltas[i]);
					} else {
						int startCodeOffset = (j - startCodes[i]) * 2;
						int currentRangeOffset = i * 2;
						int glyphIndexAddress = idRangeOffsetPointer + currentRangeOffset + idRangeOffsets[i] + startCodeOffset;
						int mark = data.position();
						data.seek(glyphIndexAddress);
						int glyph = data.readUInt16();
						data.seek(mark);
						
						characterToGlyph.put(j, glyph);
					}
					
				}
			}
		} else if (format==12) {
			int junk = data.readUInt16(); //Low bytes of format because suddenly format is a Fixed32
			int length = data.readUInt32Clamped();
			int language = data.readUInt32Clamped();
			int numSegments = data.readUInt32Clamped();
			for(int segment=0; segment<numSegments; segment++) {
				int startCode = (int)data.readUInt32();
				int endCode = (int)data.readUInt32();
				int startGlyphCode = (int)data.readUInt32();
				
				int glyphCode = startGlyphCode;
				for(int j=startCode; j<=endCode; j++) {
					characterToGlyph.put(j, glyphCode);
					glyphCode++;
				}
			}
		}
		
	}
	
	
	
	
	
	@Override
	public JsonObject toJson() {
		JsonObject result = new JsonObject();
		
		result.put("version", json(version));
		JsonArray encodingsArray = new JsonArray();
		for(CMapSubtable table : encodings) encodingsArray.add(json(TTFPlatform.getPlatformString(table.platformId, table.platformEncodingId)));
		result.put("encodings", encodingsArray);
		
		//TODO: Expand characterToGlyph
		result.put("characterToGlyph", json("("+characterToGlyph.size()+" mappings)"));
		
		return result;
	}
	
	public JsonObject toBriefJson() {
		JsonObject result = new JsonObject();
		
		result.put("version", json(version));
		JsonArray encodingsArray = new JsonArray();
		for(CMapSubtable table : encodings) encodingsArray.add(json(TTFPlatform.getPlatformString(table.platformId, table.platformEncodingId)));
		result.put("encodings", encodingsArray);
		
		result.put("characterToGlyph", json("("+characterToGlyph.size()+" mappings)"));
		
		return result;
	}
	
	private static class CMapSubtable {
		int platformId;
		int platformEncodingId;
		int offset;
		
		
	}
}
