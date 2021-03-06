/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.mesher;

import java.nio.ByteBuffer;

import org.joml.Vector3d;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryUtil;

import com.playsawdust.chipper.glow.model.Face;
import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;
import com.playsawdust.chipper.glow.model.Vertex;

public class TextMesher {
	private static Vector3d TEXT_NORMAL = new Vector3d(0, 0, -1);
	private static Material TEXT_MATERIAL = new Material.Generic()
			.with(MaterialAttribute.SPECULARITY, 0.0)
			.with(MaterialAttribute.EMISSIVITY, 1.0)
			.with(MaterialAttribute.DIFFUSE_COLOR, new Vector3d(1, 1, 1))
			.with(MaterialAttribute.DIFFUSE_TEXTURE_ID, "none");
	
	/** Gets a flat 3d mesh for the given String using stb_EasyFont */
	public static Mesh getMesh(String s) {
		ByteBuffer tempBuf = MemoryUtil.memAlloc(270*s.length()); //4 bytes per float, 4 floats per vertex, 4 vertices per character
		
		STBEasyFont.stb_easy_font_spacing(0);
		int vertices = STBEasyFont.stb_easy_font_print(0, 0, s, null, tempBuf);
		tempBuf.rewind(); //LWJGL bindings typically leave pos at zero but just to make sure
		
		Mesh mesh = new Mesh();
		mesh.setMaterial(TEXT_MATERIAL);
		for(int i=0; i<vertices; i++) {
			float x = tempBuf.getFloat();
			float y = tempBuf.getFloat();
			tempBuf.getFloat();
			tempBuf.getInt();
			Vertex a = new Vertex(new Vector3d(x, -y, 0));
			a.putMaterialAttribute(MaterialAttribute.NORMAL, TEXT_NORMAL);
			
			x = tempBuf.getFloat();
			y = tempBuf.getFloat();
			tempBuf.getFloat();
			tempBuf.getInt();
			Vertex b = new Vertex(new Vector3d(x, -y, 0));
			b.putMaterialAttribute(MaterialAttribute.NORMAL, TEXT_NORMAL);
			
			x = tempBuf.getFloat();
			y = tempBuf.getFloat();
			tempBuf.getFloat();
			tempBuf.getInt();
			Vertex c = new Vertex(new Vector3d(x, -y, 0));
			c.putMaterialAttribute(MaterialAttribute.NORMAL, TEXT_NORMAL);
			
			x = tempBuf.getFloat();
			y = tempBuf.getFloat();
			tempBuf.getFloat();
			tempBuf.getInt();
			Vertex d = new Vertex(new Vector3d(x, -y, 0));
			d.putMaterialAttribute(MaterialAttribute.NORMAL, TEXT_NORMAL);
			
			Face face = new Face(c, b, a, d);
			mesh.addFace(face);
		}
		
		
		MemoryUtil.memFree(tempBuf);
		return mesh;
	}
	
	public static Model getModel(String s) {
		Model model = new Model();
		Mesh mesh = getMesh(s);
		model.addMesh(mesh);
		return model;
	}
}
