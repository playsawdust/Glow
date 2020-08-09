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
