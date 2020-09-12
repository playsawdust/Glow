/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.gl;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.ARBTextureFilterAnisotropic;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.system.MemoryUtil;

import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.image.ImageData;
import com.playsawdust.chipper.glow.util.AbstractGPUResource;

public class Texture extends AbstractGPUResource {
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
		if (GL.getCapabilities().GL_ARB_framebuffer_object) {
			ARBFramebufferObject.glGenerateMipmap(type);
		}
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		if (GL.getCapabilities().GL_ARB_texture_filter_anisotropic) {
			int maxAnisotropy = GL11.glGetInteger(ARBTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY);
			int anisotropy = Math.min(8, maxAnisotropy);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, ARBTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY, anisotropy);
		}
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		
		MemoryUtil.memFree(buf);
		this.width = width;
		this.height = height;
	}
	
	public void bind(ShaderProgram program, String name, int texunit) {
		GL20.glActiveTexture(GL20.GL_TEXTURE0 + texunit);
		GL20.glBindTexture(type, handle);
		program.setUniform(name, texunit);
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	@Override
	public void _free() {
		if (handle!=-1) {
			GL11.glDeleteTextures(handle);
			handle = -1;
		}
	}
	
	public static Texture of(ImageData image) {
		Texture result = new Texture();
		result.uploadImage(image.getData(), image.getWidth(), image.getHeight());
		return result;
	}
}
