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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.google.common.collect.ClassToInstanceMap;
import com.playsawdust.chipper.glow.text.truetype.FontDirectionHint;
import com.playsawdust.chipper.glow.text.truetype.TTFDataInput;

import blue.endless.jankson.JsonObject;

public class TTFHead extends TTFTable {
	public static final String TAG_NAME = "head";
	public static final int TAG = tagNameToInt(TAG_NAME);
	
	public double version;
	public double fontRevision;
	public long checksumAdjustment;
	public long magic;
	public int flags;
	public int unitsPerEm;
	public long created;
	public long modified;
	public int xMin;
	public int yMin;
	public int xMax;
	public int yMax;
	public int macStyle;
	public int smallestPixelsPerEm;
	public FontDirectionHint fontDirectionHint;
	public int indexToLocFormat;
	public int glyphDataFormat;
	
	public TTFHead(int offset, int length) { super(offset, length); }
	
	@Override
	public void load(TTFDataInput data, ClassToInstanceMap<TTFTable> tables) throws IOException {
		version = data.readFixed();
		if (version!=1.0) throw new IOException("Cannot read TrueType version "+version);
		fontRevision = data.readFixed();
		checksumAdjustment = data.readUInt32(); //don't care
		magic = data.readUInt32();
		if (magic != 0x5F0F3CF5L) throw new IOException("Magic number is wrong!");
		flags = data.readUInt16();
		//TODO: Something useful with flags
		
		unitsPerEm = data.readUInt16();
		
		created = data.readLongDateTime();
		modified = data.readLongDateTime();
		
		xMin = data.readInt16();
		yMin = data.readInt16();
		xMax = data.readInt16();
		yMax = data.readInt16();
		
		macStyle = data.readUInt16();
		/*
		boolean bold = (macStyle & 0x01) != 0;
		boolean italic = (macStyle & 0x02) != 0;
		boolean underline = (macStyle & 0x04) != 0;
		boolean outline = (macStyle & 0x08) != 0;
		boolean shadow = (macStyle & 0x10) != 0;
		boolean condensed = (macStyle & 0x20) != 0;
		boolean extended = (macStyle & 0x40) != 0;*/
		
		smallestPixelsPerEm = data.readUInt16();
		
		fontDirectionHint = FontDirectionHint.of(data.readInt16());
		
		indexToLocFormat = data.readInt16();
		glyphDataFormat = data.readInt16();
		if (glyphDataFormat!=0) throw new IOException("Cannot unpack glyphs with format "+glyphDataFormat);
	}
	
	public JsonObject toJson() {
		JsonObject result = new JsonObject();
		
		result.put("version", json(version));
		result.put("fontRevision", json(fontRevision));
		result.put("checksumAdjustment", json(Long.toHexString(checksumAdjustment)));
		result.put("magic", json(Long.toHexString(magic)));
		result.put("flags", json(flags));
		result.put("unitsPerEm", json(unitsPerEm));
		result.put("created", json(timestampString(created)));
		result.put("modified", json(timestampString(modified)));
		result.put("xMin", json(xMin));
		result.put("yMin", json(yMin));
		result.put("xMax", json(xMax));
		result.put("yMax", json(yMax));
		result.put("macStyle", json(macStyle));
		result.put("smallestPixelsPerEm", json(smallestPixelsPerEm));
		result.put("fontDirectionHint", json(fontDirectionHint.toString()));
		result.put("indexToLocFormat", json(indexToLocFormat));
		result.put("glyphDataFormat", json(glyphDataFormat));
		
		return result;
	}
	
	public static String timestampString(long timestamp) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.set(1904, 1, 1, 0, 0, 0);
		while(timestamp>Integer.MAX_VALUE) {
			calendar.add(Calendar.MILLISECOND, Integer.MAX_VALUE);
			timestamp -= Integer.MAX_VALUE;
		}
		if (timestamp>0) calendar.add(Calendar.MILLISECOND, (int)timestamp);
		
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		return formatter.format(calendar.getTime());
	}
}
