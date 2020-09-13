package com.playsawdust.chipper.glow.text.truetype.table;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.collect.ClassToInstanceMap;
import com.playsawdust.chipper.glow.text.truetype.TTFDataInput;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;

public class TTFName extends TTFTable {
	public static final String TAG_NAME = "name";
	public static final int TAG = tagNameToInt(TAG_NAME);
	
	public int format;
	ArrayList<NameRecord> records = new ArrayList<>();
	
	public TTFName(int offset, int length) { super(offset, length); }
	
	public String getName(int index) {
		NameRecord record = records.get(index);
		String result = record.name;
		if (result==null) result = "";
		return result;
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
