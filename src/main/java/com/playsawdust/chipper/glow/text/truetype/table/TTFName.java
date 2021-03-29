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
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.collect.ClassToInstanceMap;
import com.playsawdust.chipper.glow.text.truetype.TTFDataInput;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonObject;

public class TTFName extends TTFTable {
	public static final String TAG_NAME = "name";
	public static final int TAG = tagNameToInt(TAG_NAME);
	
	
	/*
	 * nameId constants for use with getNameById(int)
	 */
	public static final int COPYRIGHT_NOTICE = 0;
	public static final int FONT_FAMILY = 1;
	public static final int FONT_SUBFAMILY = 2;
	public static final int UNIQUE_ID = 3;
	public static final int FULL_FONT_NAME = 4;
	public static final int VERSION = 5;
	public static final int POSTSCRIPT_NAME = 6;
	public static final int TRADEMARK = 7;
	public static final int MANUFACTURER = 8;
	public static final int DESIGNER = 9;
	public static final int DESCRIPTION = 10;
	public static final int VENDOR_URL = 11;
	public static final int DESIGNER_URL = 12;
	public static final int LICENSE = 13;
	public static final int LICENS_URL = 14;
	// 15 RESERVED
	/** USE THIS ONE */
	public static final int TYPOGRAPHIC_FAMILY_NAME = 16;
	/** USE THIS ONE. If this one is missing, use FONT_SUBFAMILY */
	public static final int TYPOGRAPHIC_SUBFAMILY_NAME = 17;
	/** Compatible with what, you ask? When the FOND resource is constructed on MacOS, it uses this (if present) to make the menu/pulldown name for this font different from FULL_FONT_NAME. */
	public static final int COMPATIBLE_FULL_NAME = 18;
	public static final int SAMPLE_TEXT = 19;
	
	//Below here are OpenType-specific 'name' IDs
	public static final int POSTSCRIPT_CID_FINDFONT_NAME = 20;
	/**
	 * This packages all options that aren't "regular", "italic", "bold", and "bold italic" into the family name instead of the subfamily name.
	 * For example, if you had a font named "Quixotic Display Semi-Bold", FONT_FAMILY would contain "Quixotic", but WWS_FAMILY_NAME would contain "Quixotic Display".
	 * (since "semi-bold" is a weight, it can go in the WWS RIBBI subfamily field.
	 */
	public static final int WWS_FAMILY_NAME = 21;
	/**
	 * This holds only subfamily qualities that are RIBBI - regular, italic, bold, bold-italic. That is, any weight information and any slant information.
	 * So for "Quixotic" (sic), this would be "Regular". For "Quixotic Display Semi-Bold" this would be "Semibold"
	 */
	public static final int WWS_SUBFAMILY_NAME = 22;
	public static final int LIGHT_BACKGROUND_PALETTE = 23;
	public static final int DARK_BACKGROUND_PALETTE = 24;
	public static final int VARIATIONS_POSTSCRIPT_NAME_INDEX = 25;
	
	public int format;
	ArrayList<NameRecord> records = new ArrayList<>();
	
	public String copyrightString;
	public String family;
	public String subfamily;
	
	
	public TTFName(int offset, int length) { super(offset, length); }
	
	public @Nullable String getName(int id) {
		for (NameRecord record : records) {
			if (record.nameId==id) {
				if (record.name!=null) return record.name;
			}
		}
		
		return null;
	}
	
	public boolean hasName(int id) {
		for(NameRecord record : records) {
			if (record.nameId==id && record.name!=null) return true;
		}
		
		return false;
	}
	
	@Override
	public void load(TTFDataInput data, ClassToInstanceMap<TTFTable> tables) throws IOException {
		int tableStart = data.position();
		
		format = data.readUInt16();
		int count = data.readUInt16();
		int stringOffset = data.readUInt16();
		
		for(int i=0; i<count; i++) {
			NameRecord record = new NameRecord();
			
			record.platformId = data.readUInt16();
			record.platformSpecificId = data.readUInt16();
			record.languageId = data.readUInt16();
			record.nameId = data.readUInt16();
			record.length = data.readUInt16();
			record.offset = data.readUInt16();
			
			records.add(record);
		}
		
		for(NameRecord record : records) {
			Charset encoding = TTFPlatform.getPlatformCharset(record.platformId, record.platformSpecificId);
			if (encoding==null) continue;
			
			data.seek(tableStart+stringOffset+record.offset);
			byte[] stringData = new byte[record.length];
			for(int i=0; i<record.length; i++) {
				stringData[i] = data.readByte();
			}
			record.name = new String(stringData, encoding);
		}
		
	}
	
	@Override
	public JsonObject toJson() {
		JsonObject result = new JsonObject();
		
		JsonArray namesArray = new JsonArray();
		for(NameRecord record : records) {
			String item = record.name;
			if (item==null) item = "(undecipherable, "+record.length+" bytes)";
			namesArray.add(json(item));
		}
		result.put("names", namesArray);
		
		return result;
	}
	
	public static class NameRecord {
		public int platformId;
		public int platformSpecificId;
		public int languageId;
		public int nameId;
		public int length;
		public int offset;
		public String name;
		
		public JsonObject toJson() {
			JsonObject result = new JsonObject();
			
			result.put("platform", json(TTFPlatform.getPlatformString(platformId, platformSpecificId)));
			result.put("languageId", json(languageId));
			result.put("nameId", json(nameId));
			result.put("length", json(length));
			result.put("offset", json(offset));
			
			return result;
			/*
			JsonElement result = Jankson.builder().build().toJson(this);
			if (result instanceof JsonObject) return (JsonObject) result;
			else return new JsonObject();*/
		}
	}
}
