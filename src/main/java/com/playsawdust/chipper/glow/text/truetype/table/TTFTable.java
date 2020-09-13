package com.playsawdust.chipper.glow.text.truetype.table;

import java.io.IOException;
import java.lang.reflect.Field;

import com.google.common.collect.ClassToInstanceMap;
import com.playsawdust.chipper.glow.text.truetype.TTFDataInput;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;

public abstract class TTFTable {
	
	protected transient int offset;
	protected transient int length;
	
	public TTFTable(int offset, int length) {
		this.offset = offset;
		this.length = length;
	}
	
	public abstract void load(TTFDataInput data, ClassToInstanceMap<TTFTable> tables) throws IOException;
	public abstract JsonObject toJson();
	
	public int getOffset() { return offset; }
	public int getLength() { return length; }
	
	public static TTFTable read(TTFDataInput in, int tag, int checksum, int offset, int length) {
		
		
		
		return null;
	}
	
	/** UNTESTED */
	public static int getTag(Class<? extends TTFTable> table) {
		try {
			Field tagField = table.getDeclaredField("TAG");
			if (tagField==null) return 0;
			return (Integer)tagField.get(null);
			
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			return 0;
		}
	}
	
	/** UNTESTED */
	public static String getTagName(Class<? extends TTFTable> table) {
		try {
			Field tagField = table.getDeclaredField("TAG_NAME");
			if (tagField==null) return "unknown";
			return (String)tagField.get(null);
			
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			return "unknown";
		}
	}
	
	protected static int tagNameToInt(String tag) {
		if (tag.length()!=4) throw new IllegalArgumentException("Not a valid tag name!");
		int a = tag.charAt(0) & 0xFF;
		int b = tag.charAt(1) & 0xFF;
		int c = tag.charAt(2) & 0xFF;
		int d = tag.charAt(3) & 0xFF;
		
		return a << 24 | b << 16 | c << 8 | d;
	}
	
	/* Helper functions to json-ize data items */
	
	protected static JsonElement json(boolean b) {
		if (b) return JsonPrimitive.TRUE;
		else return JsonPrimitive.FALSE;
	}
	
	protected static JsonElement json(String s) {
		return new JsonPrimitive(s);
	}
	
	protected static JsonElement json(int i) {
		return new JsonPrimitive((long)i);
	}
	
	protected static JsonElement json(double d) {
		return new JsonPrimitive(d);
	}
	
	protected static JsonElement json(long d) {
		return new JsonPrimitive(d);
	}
}
