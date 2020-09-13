package com.playsawdust.chipper.glow.gl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.google.common.io.ByteStreams;
import com.playsawdust.chipper.glow.event.Timestep;
import com.playsawdust.chipper.glow.image.AtlasImage;
import com.playsawdust.chipper.glow.image.ImageData;
import com.playsawdust.chipper.glow.text.Glyph;

public class Font {
	protected Font fallback;
	/**
	 * Holds CodePages which don't really stand for a particular spot in the unicode table, such as a "th" ligature,
	 * or the "rainbow flag" emoji which consists of FLAG+VS16+ZWJ+RAINBOW. Can also hold translations from shortcodes
	 * like :thonking:.
	 */
	protected ArrayList<CodePage> extendedGraphemePages = new ArrayList<>();
	/**
	 * Holds the translation table from Strings to glyphs. **This must be populated ahead of time**, as it is otherwise
	 * impossible to predict what will transform into a special glyph.
	 */
	protected HashMap<String, Glyph> extendedGraphemes = new HashMap<>();
	
	protected HashMap<Integer, CodePage> codePages = new HashMap<>();
	protected HashMap<Integer, Glyph> glyphs = new HashMap<>();
	private double spacing = 1;
	
	public Glyph getFirstGlyph(String s) {
		for(String graphemeTrigger : extendedGraphemes.keySet()) {
			if (s.startsWith(graphemeTrigger)) {
				return extendedGraphemes.get(s);
			}
		}
		
		int firstCodePoint = s.codePointAt(0);
		for(Integer glyphTrigger : glyphs.keySet()) {
			if (glyphTrigger == firstCodePoint) return glyphs.get(glyphTrigger);
		}
		
		return null; //TODO: ALWAYS return a Glyph, even if it's a missingno glyph!
	}
	
	public double getGlyphSpacing() {
		return spacing;
	}
	
	public double getGlyphSpacing(Glyph a, Glyph b) {
		return spacing;
	}
	
	public void foo() {
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
	
	
	
	private class CodePage {
		private AtlasImage atlas;
		
		public ImageData getImage() {
			return atlas.getImage();
		}
	}
}
