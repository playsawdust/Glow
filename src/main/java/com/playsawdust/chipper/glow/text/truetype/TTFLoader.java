package com.playsawdust.chipper.glow.text.truetype;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.io.ByteStreams;
import com.playsawdust.chipper.glow.event.Timestep;
import com.playsawdust.chipper.glow.text.truetype.table.TTFCharacterMap;
import com.playsawdust.chipper.glow.text.truetype.table.TTFHead;
import com.playsawdust.chipper.glow.text.truetype.table.TTFHorizontalHeader;
import com.playsawdust.chipper.glow.text.truetype.table.TTFHorizontalMetrics;
import com.playsawdust.chipper.glow.text.truetype.table.TTFLocateGlyphs;
import com.playsawdust.chipper.glow.text.truetype.table.TTFMaximumProfile;
import com.playsawdust.chipper.glow.text.truetype.table.TTFName;
import com.playsawdust.chipper.glow.text.truetype.table.TTFTable;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;

public class TTFLoader {
	
	public static void loadSTB() {
		long start = Timestep.now();
		System.out.println("Loading font");
		try(MemoryStack stack = MemoryStack.stackPush()) {
			STBTTFontinfo info = STBTTFontinfo.mallocStack();
			FileInputStream in = new FileInputStream("Roboto-Medium.ttf");
			byte[] fontData = ByteStreams.toByteArray(in);
			ByteBuffer fontDataBuffer = MemoryUtil.memAlloc(fontData.length);
			fontDataBuffer.put(fontData);
			fontDataBuffer.flip();
			
			STBTruetype.stbtt_InitFont(info, fontDataBuffer);
			
			IntBuffer ascentBuf = stack.mallocInt(1);
			IntBuffer descentBuf = stack.mallocInt(1);
			IntBuffer lineGapBuf = stack.mallocInt(1);
			
			STBTruetype.stbtt_GetFontVMetrics(info, ascentBuf, descentBuf, lineGapBuf);
			
			double points = 12.0;
			double ascent = ascentBuf.get();
			double descent = descentBuf.get();
			double lineGap = lineGapBuf.get();
			double scalingFactor = points / (ascent - descent);
			double scaledAscent = ascent * scalingFactor;
			double scaledDescent = descent * scalingFactor;
			double scaledLineGap = lineGap * scalingFactor;
			
			System.out.println("Font ascent: "+scaledAscent+" descent: "+scaledDescent+" lineGap: "+scaledLineGap);
			
			IntBuffer x0 = stack.mallocInt(1);
			IntBuffer y0 = stack.mallocInt(1);
			IntBuffer x1 = stack.mallocInt(1);
			IntBuffer y1 = stack.mallocInt(1);
			STBTruetype.stbtt_GetFontBoundingBox(info, x0, y0, x1, y1);
			
			double x1Scaled = x0.get() * scalingFactor;
			double y1Scaled = y0.get() * scalingFactor;
			double x2Scaled = x1.get() * scalingFactor;
			double y2Scaled = y1.get() * scalingFactor;
			
			int widthScaled = (int) Math.ceil(x2Scaled-x1Scaled);
			int heightScaled = (int) Math.ceil(y2Scaled-y1Scaled);
			
			System.out.println("All possible characters fit inside ("+x1Scaled+", "+y1Scaled+"), ("+widthScaled+" x "+heightScaled+")");
			
			IntBuffer advanceWidthBuf = stack.mallocInt(1);
			IntBuffer leftSideBearingBuf = stack.mallocInt(1);
			STBTruetype.stbtt_GetCodepointHMetrics(info, 'a', advanceWidthBuf, leftSideBearingBuf);
			
			System.out.println("'a' > advanceWidth: "+advanceWidthBuf.get()+" leftSideBearing: "+leftSideBearingBuf.get());
			
			
			
			MemoryUtil.memFree(fontDataBuffer);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("Finished loading, "+(Timestep.now()-start)+" msec elapsed");
	}
	
	
	public static void load(InputStream in) throws IOException {
		long start = Timestep.now();
		
		//TTF somewhat requires random access, so if what we have is an InputStream, read it all in in advance
		TTFDataInput data = new TTFDataInput(ByteStreams.toByteArray(in));
		
		int scalerType = data.readUInt32Clamped();
		if (scalerType!=0x10000 && scalerType!=0x74727565) {
			throw new IOException("Non-Truetype Font (type 0x"+Integer.toHexString(scalerType)+")");
		}
		
		int numTables = data.readUInt16();
		System.out.println("NumTables: "+numTables);
		
		/** Intended to facilitate binary searches of the table directory, which we don't care about */
		int searchRange = data.readUInt16();
		int entrySelector = data.readUInt16();
		int rangeShift = data.readUInt16();
		ArrayList<Table> tables = new ArrayList<>();
		
		ClassToInstanceMap<TTFTable> generatedTables = MutableClassToInstanceMap.create();
		//REQUIRED TABLES
		TTFHead              headTable = null;
		TTFHorizontalHeader  hheaTable = null;
		TTFHorizontalMetrics hmtxTable = null;
		TTFCharacterMap      cmapTable = null;
		TTFMaximumProfile    maxpTable = null;
		TTFName              nameTable = null;
		TTFLocateGlyphs      locaTable = null;
		
		for(int i=0; i<numTables; i++) {
			Table table = new Table();
			table.tag = data.readUInt32Clamped();
			table.checksum = data.readUInt32Clamped();
			table.offset = data.readUInt32Clamped();
			table.length = data.readUInt32Clamped();
			
			tables.add(table);
		}
		
		//JsonObject metadata = new JsonObject();
		
		
		for(Table table : tables) {
			data.seek(table.offset);
			System.out.println("Reading in Table "+debugTag(table.tag)+" ("+table.length+" bytes)");
			if (table.tag==TTFHead.TAG) {
				
				headTable = new TTFHead(table.offset, table.length);
				headTable.load(data, generatedTables);
				generatedTables.put(TTFHead.class, headTable);
				
			} else if (table.tag==TTFName.TAG) {
				
				nameTable = new TTFName(table.offset, table.length);
				nameTable.load(data, generatedTables);
				generatedTables.put(TTFName.class, nameTable);
				
			} else if (table.tag==TTFHorizontalHeader.TAG) {
				
				hheaTable = new TTFHorizontalHeader(table.offset, table.length);
				hheaTable.load(data, generatedTables);
				generatedTables.put(TTFHorizontalHeader.class, hheaTable);
				
			} else if (table.tag==TTFHorizontalMetrics.TAG) {
				
				hmtxTable = new TTFHorizontalMetrics(table.offset, table.length);
				hmtxTable.load(data, generatedTables);
				generatedTables.put(TTFHorizontalMetrics.class, hmtxTable);
				
			} else if (table.tag==TTFCharacterMap.TAG) {
				
				cmapTable = new TTFCharacterMap(table.offset, table.length);
				cmapTable.load(data, generatedTables);
				generatedTables.put(TTFCharacterMap.class, cmapTable);
				
			} else if (table.tag==TTFMaximumProfile.TAG) {
				
				maxpTable = new TTFMaximumProfile(table.offset, table.length);
				maxpTable.load(data, generatedTables);
				generatedTables.put(TTFMaximumProfile.class, maxpTable);
				
			} else if (table.tag==TTFLocateGlyphs.TAG) {
				
				locaTable = new TTFLocateGlyphs(table.offset, table.length);
				locaTable.load(data, generatedTables);
				generatedTables.put(TTFLocateGlyphs.class, locaTable);
				
			} else {
				System.out.println("  Skipping unknown table type.");
			}
		}
		
		if (headTable==null) throw new IOException("Missing required table 'head'");
		if (hheaTable==null) throw new IOException("Missing required table 'hhea'");
		if (hmtxTable==null) throw new IOException("Missing required table 'hmtx'");
		if (cmapTable==null) throw new IOException("Missing required table 'cmap'");
		if (maxpTable==null) throw new IOException("Missing required table 'maxp'");
		if (nameTable==null) throw new IOException("Missing required table 'name'");
		if (locaTable==null) throw new IOException("Missing required table 'loca'");
		
		// *** Copy data from TTF-specific structs out to metadata and metrics ***
		/*
		metadata.put("format", json("TTF"));
		metadata.put("ttfVersion", json(headTable.version));
		metadata.put("fontRevision", json(headTable.fontRevision));
		metadata.put("created", json(timestampString(headTable.created)));
		metadata.put("modified", json(timestampString(headTable.modified)));
		metadata.put("direction", json(headTable.fontDirectionHint.toString()));
		*/
		
		
		// Debug!
		/*
		JsonObject overview = new JsonObject();
		overview.put("head", headTable.toJson());
		overview.put("hhea", hheaTable.toJson());
		overview.put("hmtx", hmtxTable.toBriefJson());
		overview.put("cmap", cmapTable.toBriefJson());
		overview.put("maxp", maxpTable.toJson());
		overview.put("name", nameTable.toJson());
		overview.put("loca", locaTable.toJson());
		System.out.println("Overview: "+overview.toJson(JsonGrammar.JSON5));
		*/
		
		
		in.close();
		System.out.println("Finished loading, "+(Timestep.now()-start)+" msec elapsed");
	}
	
	private static JsonElement json(boolean b) {
		if (b) return JsonPrimitive.TRUE;
		else return JsonPrimitive.FALSE;
	}
	
	private static JsonElement json(String s) {
		return new JsonPrimitive(s);
	}
	
	private static JsonElement json(int i) {
		return new JsonPrimitive((long)i);
	}
	
	private static JsonElement json(double d) {
		return new JsonPrimitive(d);
	}
	
	private static String debugTag(int tag) {
		int a = (tag >> 24) & 0xFF;
		int b = (tag >> 16) & 0xFF;
		int c = (tag >>  8) & 0xFF;
		int d = tag & 0xFF;
		
		return ""+(char)a+(char)b+(char)c+(char)d;
	}
	
	private static int tagNameToInt(String tag) {
		if (tag.length()!=4) throw new IllegalArgumentException("Not a valid tag name!");
		int a = tag.charAt(0) & 0xFF;
		int b = tag.charAt(1) & 0xFF;
		int c = tag.charAt(2) & 0xFF;
		int d = tag.charAt(3) & 0xFF;
		
		return a << 24 | b << 16 | c << 8 | d;
	}
	
	private static class Table {
		private int tag;
		private int checksum;
		private int offset;
		private int length;
	}
	
	
	
	public static @interface FontUnits {
	}
	
	public static class FontMetrics {
		/** The conversion ratio from a "Font Unit" to an Em, useful for deriving font sizes */
		private double unitsPerEm;
		/** The minimum size of this font, in Pixels per Em (9.0 here would mean that "9 point" is the minimum intelligible size for this font) */
		private double smallestPixelsPerEm;
		
		private @FontUnits int xMin;
		private @FontUnits int yMin;
		private @FontUnits int xMax;
		private @FontUnits int yMax;
		
		/** The highest coordinate of any part of the font (+y is up!)*/
		private @FontUnits int ascent;
		/** The lowest coordinate of any part of the font (+y is up, meaning this is often negative!)*/
		private @FontUnits int descent;
		/** Extra space to be inserted between lines */
		private @FontUnits int lineGap;
		/** Maximum stride of any character */
		private @FontUnits int maxAdvanceWidth;
		/** Minimum amount of glyph hanging off the left side of a character */
		private @FontUnits int minLeftSideBearing;
		/** Minimum amount of glyph hanging off the right side of a character */
		private @FontUnits int minRightSideBearing;
		/** max(leftSideBearing + (xMax-xMin)), probably useless and should be moved to Non-Normative Data */
		private @FontUnits int maxXExtent;
		
		public double getPixelsPerUnit(double fontSizeInPoints) {
			double pixelsPerEm = fontSizeInPoints;
			double emsPerUnit = 1.0 / (double) unitsPerEm;
			return pixelsPerEm * emsPerUnit;
		}
		
		public double getAdvanceWidth(double fontSizePoints) {
			return maxAdvanceWidth * getPixelsPerUnit(fontSizePoints);
		}
		
		public double getAscent(double fontSizeInPoints) {
			return ascent * getPixelsPerUnit(fontSizeInPoints);
		}
		
		public double getDescent(double fontSizeInPoints) {
			return descent * getPixelsPerUnit(fontSizeInPoints);
		}
		
		public JsonObject toJson() {
			JsonObject result = new JsonObject();
			result.put("unitsPerEm", json(unitsPerEm));
			result.put("smallestPixelsPerEm", json(smallestPixelsPerEm));
			result.put("xMin", json(xMin));
			result.put("yMin", json(yMin));
			result.put("xMax", json(xMax));
			result.put("yMax", json(yMax));
			
			result.put("ascent", json(ascent));
			result.put("descent", json(descent));
			result.put("lineGap", json(lineGap));
			result.put("maxAdvanceWidth", json(maxAdvanceWidth));
			result.put("minLeftSideBearing", json(minLeftSideBearing));
			result.put("minRightSideBearing", json(minRightSideBearing));
			result.put("maxXExtent", json(maxXExtent));
			return result;
		}
	}
	
	
	
	private static String timestampString(long timestamp) {
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
	
	/*
	private static class RandomAccessData {
		private static final double FIXED_CONVERSION_FACTOR = Math.pow(2.0, 16.0);
		
		private byte[] data;
		private int pointer;
		
		public RandomAccessData(byte[] data) { this.data = data; }
		
		public void seek(int offset) { //TODO: Range check and throw
			this.pointer = offset;
		}
		
		public int offset() {
			return this.pointer;
		}
		
		public int readUByte() {
			int result = data[pointer] & 0xFF;
			pointer++;
			return result;
		}
		
		
		public short readInt16() {
			int hi = readUByte();
			int lo = readUByte();
			
			return (short) (hi << 8 | lo);
		}
		
		public int readUInt16() {
			int hi = readUByte();
			int lo = readUByte();
			return hi << 8 | lo;
		}
		
		public int readUInt32() {
			long result = safeReadUInt32();
			
			return (result>Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) result;
		}
		
		public long safeReadUInt32() {
			long a = readUByte();
			long b = readUByte();
			long c = readUByte();
			long d = readUByte();
			
			return a << 24 | b << 16 | c << 8 | d;
		}
		
		public double readFixed32() {
			long bits = safeReadUInt32();
			long whole = bits >> 16;
			double fractional = (bits & 0xFFFF) / FIXED_CONVERSION_FACTOR;
			//System.out.println("Fractional : " + Integer.toHexString((int)bits & 0xFFFF));
			return whole + fractional;
			
			//double bits = safeReadUInt32();
			//return bits / FIXED_CONVERSION_FACTOR; //TODO: Probably bitshift values in so we don't gain phantom precision like this
		}
		
		public long readInt64() {
			long a = readUByte();
			long b = readUByte();
			long c = readUByte();
			long d = readUByte();
			
			long e = readUByte();
			long f = readUByte();
			long g = readUByte();
			long h = readUByte();
			
			return 
				a << 56 |
				b << 48 |
				c << 40 |
				d << 32 |
				e << 24 |
				f << 16 |
				g <<  8 |
				h;
		}
		
		public long readLongDate() {
			long timestamp = readInt64() * 1_000; //Seconds to millis
			return timestamp;
		}
	}*/
}
