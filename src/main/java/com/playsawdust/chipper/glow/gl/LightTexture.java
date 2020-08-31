/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.gl;

import java.util.ArrayList;
import java.util.Iterator;

import org.lwjgl.opengl.ARBTextureFloat;
import org.lwjgl.opengl.ARBTextureRectangle;
import org.lwjgl.opengl.GL20;

import com.playsawdust.chipper.glow.gl.shader.Destroyable;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.scene.Light;

public class LightTexture implements Iterable<Light>, Destroyable {
	private static final int LIGHT_LENGTH = 12;
	
	private ArrayList<Light> lights = new ArrayList<>();
	private int handle = -1;
	
	public void addLight(Light light) {
		lights.add(light);
	}
	
	public void removeLight(Light light) {
		lights.remove(light);
	}
	
	public int size() {
		return lights.size();
	}
	
	public Light getLight(int index) {
		return lights.get(index);
	}
	
	public void upload() {
		if (handle!=-1) {
			GL20.glDeleteTextures(handle); //TODO: Resize and use glTexSubImage!	
			handle = -1;
		}
		int lightCount = lights.size();
		if (lightCount==0) lightCount = 1;
		int dataLength = lightCount*LIGHT_LENGTH;
		float[] data = new float[dataLength];
		for(int i=0; i<lights.size(); i++) {
			Light cur = lights.get(i);
			data[LIGHT_LENGTH*i + 0] = (float) cur.getPosition(null).x();
			data[LIGHT_LENGTH*i + 1] = (float) cur.getPosition(null).y();
			data[LIGHT_LENGTH*i + 2] = (float) cur.getPosition(null).z();
			data[LIGHT_LENGTH*i + 3] = (float) cur.getRadius();
			
			data[LIGHT_LENGTH*i + 4] = (float) cur.getDirection().x();
			data[LIGHT_LENGTH*i + 5] = (float) cur.getDirection().y();
			data[LIGHT_LENGTH*i + 6] = (float) cur.getDirection().z();
			data[LIGHT_LENGTH*i + 7] = (float) cur.getAngle();
			
			data[LIGHT_LENGTH*i + 8] = (float) (cur.getColor().x()*cur.getIntensity());
			data[LIGHT_LENGTH*i + 9] = (float) (cur.getColor().y()*cur.getIntensity());
			data[LIGHT_LENGTH*i +10] = (float) (cur.getColor().z()*cur.getIntensity());
			data[LIGHT_LENGTH*i +11] = (float) cur.getIntensity();
		}
		
		handle = GL20.glGenTextures();
		GL20.glBindTexture(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, handle);
		GL20.glTexImage2D(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, 0, ARBTextureFloat.GL_RGBA32F_ARB, 3, lightCount, 0, GL20.GL_RGBA, GL20.GL_FLOAT, data);
	}
	
	public void bind(ShaderProgram program, int texunit) {
		GL20.glActiveTexture(GL20.GL_TEXTURE0 + texunit);
		GL20.glBindTexture(ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB, handle);
		program.setUniform("lights", texunit);
	}
	
	@Override
	public Iterator<Light> iterator() {
		return lights.iterator();
	}

	@Override
	public void destroy() {
		if (handle!=-1) {
			GL20.glDeleteTextures(handle);
			handle = -1;
		}
	}
}
