package com.playsawdust.chipper.glow.gl;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.system.MemoryUtil;

import com.playsawdust.chipper.glow.gl.shader.Destroyable;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;

public class Texture implements Destroyable {
	private int handle = -1;
	private int type = GL11.GL_TEXTURE_2D;
	private int width = 0;
	private int height = 0;
	
	
	public Texture() {
		handle = GL11.glGenTextures();
	}
	
	/**
	 * Upload image data in srgb format
	 * @param image
	 */
	public void uploadImage(int[] image, int width, int height) {
		type = GL11.GL_TEXTURE_2D;
		ByteBuffer buf = MemoryUtil.memAlloc(width*height*4);
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				int sampledY = (height-y) - 1;
				buf.putInt(image[sampledY*width + x]);
			}
		}
		buf.flip();
		GL20.glActiveTexture(GL20.GL_TEXTURE0);
		GL20.glBindTexture(type, handle);
		GL11.glTexImage2D(type, 0, GL21.GL_RGBA, width, height, 0, GL21.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buf);
		MemoryUtil.memFree(buf);
		this.width = width;
		this.height = height;
	}
	
	public void bind(ShaderProgram program, String name, int texunit) {
		GL20.glActiveTexture(GL20.GL_TEXTURE0 + texunit);
		GL20.glBindTexture(type, handle);
		program.setUniform(name, texunit);
	}
	
	@Override
	public void destroy() {
		if (handle!=-1) {
			GL11.glDeleteTextures(handle);
			handle = -1;
		}
	}
	
	
}
