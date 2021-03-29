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

import com.google.common.collect.ClassToInstanceMap;
import com.playsawdust.chipper.glow.text.truetype.TTFDataInput;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;

public class TTFMaximumProfile extends TTFTable {
	public static final String TAG_NAME = "maxp";
	public static final int TAG = tagNameToInt(TAG_NAME);
	
	public double version;
	public int numGlyphs;
	public int maxPoints;
	public int maxContours;
	public int maxComponentPoints;
	public int maxComponentContours;
	public int maxZones; //Should be 2
	public int maxTwilightPoints; //This is the maximum points in the Twilight Zone. I'm not even kidding.
	
	//Hinting language
	public int maxStorage;
	public int maxFunctionDefs;
	public int maxInstructionDefs;
	public int maxStackElements;
	public int maxSizeOfInstructions;
	
	//Nested glyphs
	public int maxComponentElements;
	public int maxComponentDepth;
	
	public TTFMaximumProfile(int offset, int length) { super(offset, length); }
	
	@Override
	public void load(TTFDataInput data, ClassToInstanceMap<TTFTable> tables) throws IOException {
		version = data.readFixed();
		if (version==0.5) throw new IOException("Can't read OpenType files with PostScript outlines."); // If we can describe what's wrong in a way that makes sense to the user, do.
		if (version!=1.0) throw new IOException("Can't read maxp version "+version);
		numGlyphs = data.readUInt16();
		maxPoints = data.readUInt16();
		maxContours = data.readUInt16();
		maxComponentPoints = data.readUInt16();
		maxComponentContours = data.readUInt16();
		maxZones = data.readUInt16();
		maxTwilightPoints = data.readUInt16();
		maxStorage = data.readUInt16();
		maxFunctionDefs = data.readUInt16();
		maxStackElements = data.readUInt16();
		maxSizeOfInstructions = data.readUInt16();
		maxComponentElements = data.readUInt16();
		maxComponentDepth = data.readUInt16();
	}

	@Override
	public JsonObject toJson() {
		JsonElement result = Jankson.builder().build().toJson(this);
		if (result instanceof JsonObject) return (JsonObject) result;
		else return new JsonObject();
	}

}
