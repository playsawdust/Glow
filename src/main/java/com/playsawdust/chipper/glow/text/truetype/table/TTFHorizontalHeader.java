package com.playsawdust.chipper.glow.text.truetype.table;

import java.io.IOException;

import com.google.common.collect.ClassToInstanceMap;
import com.playsawdust.chipper.glow.text.truetype.TTFDataInput;

import blue.endless.jankson.JsonObject;

public class TTFHorizontalHeader extends TTFTable {
	public static final String TAG_NAME = "hhea";
	public static final int TAG = tagNameToInt(TAG_NAME);
	
	public double version;
	public int ascent;
	public int descent;
	public int lineGap;
	public int maxAdvanceWidth;
	public int minLeftSideBearing;
	public int minRightSideBearing;
	public int maxXExtent;
	public int caretSlopeRise;
	public int caretSlopeRun;
	public int caretOffset;
	public int metricDataFormat;
	public int horizontalMetricsCount;
	
	public TTFHorizontalHeader(int offset, int length) { super(offset, length); }
	
	@Override
	public void load(TTFDataInput data, ClassToInstanceMap<TTFTable> tables) throws IOException {
		version = data.readFixed();
		if (version!=1.0) throw new IOException("Cannot read horizontal metrics version "+version);
		
		ascent = data.readInt16();
		descent = data.readInt16();
		lineGap = data.readInt16();
		maxAdvanceWidth = data.readUInt16();
		minLeftSideBearing = data.readInt16();
		minRightSideBearing = data.readInt16();
		maxXExtent = data.readInt16();
		caretSlopeRise = data.readInt16();
		caretSlopeRun = data.readInt16();
		caretOffset = data.readInt16();
		data.readInt16(); //reserved
		data.readInt16();
		data.readInt16();
		data.readInt16();
		metricDataFormat = data.readInt16();
		if (metricDataFormat!=0) throw new IOException("Don't understand horizontal metrics format version "+metricDataFormat);
		horizontalMetricsCount = data.readUInt16();
	}
	
	@Override
	public JsonObject toJson() {
		JsonObject result = new JsonObject();
		
		result.put("version", json(version));
		result.put("ascent", json(ascent));
		result.put("descent", json(descent));
		result.put("lineGap", json(lineGap));
		result.put("maxAdvanceWidth", json(maxAdvanceWidth));
		result.put("minLeftSideBearing", json(minLeftSideBearing));
		result.put("minRightSideBearing", json(minRightSideBearing));
		result.put("maxXExtent", json(maxXExtent));
		result.put("caretSlopeRise", json(caretSlopeRise));
		result.put("caretSlopeRun", json(caretSlopeRun));
		result.put("caretOffset", json(caretOffset));
		result.put("metricDataFormat", json(metricDataFormat));
		result.put("horizontalMetricsCount", json(horizontalMetricsCount));
		
		return result;
	}

	
	
}
