/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.gl.shader;

public class NodeShader {
	public static final String PREAMBLE =
			"precision mediump float;\n" + 
			"\n" + 
			"attribute vec3 inPos;\n" + 
			"attribute vec3 inNV;\n" + 
			"attribute vec3 inCol;\n" + 
			"\n" + 
			"varying vec3 vertPos;\n" + 
			"varying vec3 vertNV;\n" + 
			"varying vec3 vertCol;\n" + 
			"\n" + 
			"uniform mat4 u_projectionMat44;\n" + 
			"uniform mat4 u_viewMat44;\n" + 
			"uniform mat4 u_modelMat44;\n" + 
			"";
}
