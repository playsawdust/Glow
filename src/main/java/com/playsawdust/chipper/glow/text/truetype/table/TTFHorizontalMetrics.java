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

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonObject;

public class TTFHorizontalMetrics extends TTFTable {
	public static final String TAG_NAME = "hmtx";
	public static final int TAG = tagNameToInt(TAG_NAME);
	
	public ArrayList<GlyphMetrics> metrics = new ArrayList<>();
	
	public TTFHorizontalMetrics(int offset, int length) { super(offset, length); }
	
	@Override
	public void load(TTFDataInput data, ClassToInstanceMap<TTFTable> tables) throws IOException {
		TTFHorizontalHeader hheaTable = tables.getInstance(TTFHorizontalHeader.class);
		if (hheaTable==null) throw new IOException("Missing required table 'hhea'");
		if (hheaTable.horizontalMetricsCount<0) throw new IOException("Broken horizontal metrics count");
		
		int lastAdvanceWidth = 0;
		for(int i=0; i<hheaTable.horizontalMetricsCount; i++) {
			GlyphMetrics glyphMetrics = new GlyphMetrics();
			glyphMetrics.advanceWidth = data.readUInt16();
			glyphMetrics.leftSideBearing = data.readInt16();
			
			metrics.add(glyphMetrics);
			
			lastAdvanceWidth = glyphMetrics.advanceWidth;
		}
		
		//TODO: There MAY be an array of raw leftSideBearings here. But we need the glyph map to know!
		//Basically though if there's extra room we should treat it as either padding or extra leftSideBearings using lastAdvanceWidth for width.
	}

	@Override
	public JsonObject toJson() {
		JsonObject result = new JsonObject();
		
		JsonArray metricsArray = new JsonArray();
		for(GlyphMetrics m : metrics) {
			metricsArray.add(m.toJson());
		}
		result.put("metrics", metricsArray);
		
		return result;
	}
	
	
	public JsonObject toBriefJson() {
		JsonObject result = new JsonObject();
		
		result.put("metrics", json("("+metrics.size()+" glyph widths+bearings)"));
		
		return result;
	}
	
	public static class GlyphMetrics {
		int advanceWidth;
		int leftSideBearing;
		
		public JsonObject toJson() {
			JsonObject result = new JsonObject();
			result.put("advanceWidth", json(advanceWidth));
			result.put("leftSideBearing", json(leftSideBearing));
			
			return result;
		}

		public int getAdvanceWidth() {
			return advanceWidth;
		}
		
		public int getLeftSideBearing() {
			return leftSideBearing;
		}
	}
}
