package com.playsawdust.chipper.glow.image.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.google.common.io.ByteStreams;
import com.playsawdust.chipper.glow.image.ImageData;

public class ImageLoader {
	
	/**
	 * Loads an image of unknown type using STBImage. Has some color bugs when decoding PNGs!
	 * @param in InputStream which whill provide image data
	 * @return a ClientImage containing the decoded image
	 * @throws IOException if there is a problem reading from the stream or decoding the image
	 */
	public static ImageData load(InputStream in) throws IOException {
		byte[] array;
		array = ByteStreams.toByteArray(in);
		in.close();
		
		//Do feature detection
		int shortMagic = (array[0]&0xFF) << 8 | (array[1]&0xFF);
		
		if (shortMagic==PNGImageLoader.PNG_SHORTMAGIC) {
			try {
				return PNGImageLoader.load(new ByteArrayInputStream(array)); //Bail and use the java loader if we can
			} catch (IOException ex) {
				//Fallback to STBi, which (improperly) loads a wider range of PNG variants
			}
		}
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			ByteBuffer buf = stack.malloc(array.length);
			IntBuffer x = stack.mallocInt(1);
			IntBuffer y = stack.mallocInt(1);
			IntBuffer channelsInFile = stack.mallocInt(1);
			buf.put(array);
			buf.flip();
			
			ByteBuffer imageBuf = STBImage.stbi_load_from_memory(buf, x, y, channelsInFile, 4);
			if (imageBuf==null) {
				throw new IOException(STBImage.stbi_failure_reason());
			}
			
			int width = x.get();
			int height = y.get();
			
			int[] data = new int[width*height];
			imageBuf.position(0);
			for(int i=0; i<data.length; i++) {
				int r = imageBuf.get();
				int g = imageBuf.get();
				int b = imageBuf.get();
				int a = imageBuf.get();
				
				data[i] =
					a << 24 |
					r << 16 |
					g << 8 |
					b;
			}
			ImageData image = new ImageData(width, height, data);
			MemoryUtil.memFree(imageBuf);
			return image;
		}
	}
	
	
}
