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
			
		#version 150
		
		//material
		uniform vec3 materialColor = vec3(1, 1, 1);
		uniform float materialSpecularity = 1.0;
		uniform float materialEmissivity = 0.0;
		
		//view
		uniform mat4 modelMatrix;
		uniform mat4 viewMatrix;
		uniform sampler2DRect lights;
		uniform vec3 ambientLight;
		
		attribute vec4 position;
		attribute vec3 normal;
		attribute vec2 uv;
		
		varying vec3 v2f_normal;
		varying float v2f_diffuse;
		varying vec3 v2f_vertexColor;
		varying vec2 v2f_uv;
		
		
		void main() {
			vec3 worldSpaceVertex = (modelMatrix * position).xyz;
			vec3 worldSpaceNormal = normalize((mat3(modelMatrix) * normal).xyz);
			vec3 cameraSpaceVertex = (viewMatrix * vec4(worldSpaceVertex, 1.0)).xyz;
			vec3 cameraVector = normalize(cameraSpaceVertex);
			
			v2f_uv = uv;
			
			int lightCount = textureSize(lights).y;
			v2f_vertexColor = ambientLight;
			for(int i=0; i<lightCount; i++) {
				vec4 lightPositionRadius = texture(lights, vec2(0.5, i+0.5));
				vec4 lightDirectionAngle = texture(lights, vec2(1.5, i+0.5));
				vec4 lightColorAlpha = texture(lights, vec2(2.5, i+0.5));
				
				float distanceScale = 1 - clamp(length(lightPositionRadius.xyz - worldSpaceVertex) / lightPositionRadius.w, 0.0, 1.0);
				
				vec3 lightColor = lightColorAlpha.xyz;
				
				vec3 lightVector = normalize(lightPositionRadius.xyz - worldSpaceVertex);
				float diffuseStrength = clamp( dot(worldSpaceNormal, lightVector), 0.0, 1.0);
				//TODO: Distance falloff
				
				diffuseStrength *= distanceScale;
				
				v2f_vertexColor += (lightColor * diffuseStrength) * materialColor;
				
				//Specular
				vec3 reflectedLight = normalize(reflect(-lightVector, worldSpaceNormal));
				float cosAlpha = clamp( dot(cameraVector, reflectedLight), 0.0, 1.0);
				
				v2f_vertexColor += lightColor * materialColor * pow(cosAlpha, 5) * lightColorAlpha.w * materialSpecularity * distanceScale;
			}
			vec3 emissivityFloor = materialColor*materialEmissivity;
			v2f_vertexColor.x = max(v2f_vertexColor.x, emissivityFloor.x);
			v2f_vertexColor.y = max(v2f_vertexColor.y, emissivityFloor.y);
			v2f_vertexColor.z = max(v2f_vertexColor.z, emissivityFloor.z);
		
			v2f_normal = normal;
			gl_Position = viewMatrix * modelMatrix * position;
		}
	
	""";
	
	
	public static final String SHADER_SOLID_FRAGMENT = """
		
		#version 150
		
		uniform sampler2D materialDiffuseTexture;
		
		varying vec3 v2f_normal;
		varying float v2f_diffuse;
		varying vec3 v2f_vertexColor;
		varying vec2 v2f_uv;
		
		void main() {
			vec4 fragColor = vec4(v2f_vertexColor, 1.0);
			vec4 diffuseTexture = texture(materialDiffuseTexture, v2f_uv);
			
			//fragColor.x += v2f_uv.x/2;
			//fragColor.y += v2f_uv.y/2;
			
			fragColor.x *= diffuseTexture.x;
			fragColor.y *= diffuseTexture.y;
			fragColor.z *= diffuseTexture.z;
			
			gl_FragColor = fragColor;
		}
		
	""";
	
	public static final String SHADER_PAINTER_VERTEX = """
		
		#version 150
					
		uniform mat4 viewMatrix;
		
		attribute vec2 position;
		attribute vec2 uv;
		attribute vec4 color;
		
		varying vec2 v2f_uv;
		varying vec4 v2f_color;
		
		void main() {
			gl_Position = viewMatrix * vec4(position, 0, 1);
			v2f_uv = uv;
			v2f_color = color;
		}
		
	""";
	
	public static final String SHADER_PAINTER_FRAGMENT = """
		
		#version 150
		
		uniform sampler2D tex;
		
		varying vec2 v2f_uv;
		varying vec4 v2f_color;
		
		void main() {
			//gl_FragColor = vec4(v2f_uv, 0, 1);
			gl_FragColor = texture(tex, v2f_uv) * v2f_color;
		}
		
	""";
	
	public static ShaderProgram createSolidShader() {
		try {
			return new ShaderProgram(SHADER_SOLID_VERTEX, SHADER_SOLID_FRAGMENT);
		} catch (ShaderException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static ShaderProgram createPainterShader() {
		try {
			return new ShaderProgram(SHADER_PAINTER_VERTEX, SHADER_PAINTER_FRAGMENT);
		} catch (ShaderException e) {
			throw new RuntimeException(e);
		}
	}
}
