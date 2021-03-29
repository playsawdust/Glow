/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.gl.shader;

/**
 * This class holds pre-written shaders that are appropriate for most general rendering cases.
 * These shaders are compiled and used in the default render scheduler.
 * 
 * <p>Once node shaders are online, this class may also hold the GLSL functions for some common nodes,
 * or glue code / helper functions to wire the functions together.
 */
public class BuiltinShaders {
	public static final String SHADER_SOLID_VERTEX = """
			Stuff
			""";
	
	
	public static final String SHADER_SOLID_FRAGMENT = "";
	
	
	public ShaderProgram createSolidShader() {
		try {
			return new ShaderProgram(SHADER_SOLID_VERTEX, SHADER_SOLID_FRAGMENT);
		} catch (ShaderException e) {
			throw new RuntimeException(e);
		}
	}
}
