/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.model.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joml.Vector2d;
import org.joml.Vector3d;

import com.playsawdust.chipper.glow.mesher.PlatonicSolidMesher;
import com.playsawdust.chipper.glow.model.Face;
import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;
import com.playsawdust.chipper.glow.model.Vertex;
import com.playsawdust.chipper.glow.model.boxanim.BoxBone;
import com.playsawdust.chipper.glow.model.boxanim.BoxModel;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;

public class BBModelLoader {
	public static BoxModel load(InputStream in) throws IOException {
		try {
			JsonObject bbJson = Jankson.builder().build().load(in);
			JsonObject metaObject = bbJson.getObject("meta");
			if (metaObject==null || !"3.6".equals(metaObject.get(String.class, "format_version"))) throw new IOException("Not a valid bbmodel");
			
			System.out.println("Model name: "+bbJson.get(String.class, "name"));
			
			//This is a valid '.bbmodel' for a version of blockbench that we understand
			
			
			JsonArray elements = bbJson.get(JsonArray.class, "elements");
			if (elements==null) throw new IOException("Empty model.");
			
			int meshNumber = 0;
			BoxModel result = new BoxModel();
			ArrayList<Material> materials = new ArrayList<>();
			
			for(JsonElement element : elements) {
				if (element instanceof JsonObject) {
					String boneName = "mesh_"+meshNumber; meshNumber++;
					Model model = unpackElement((JsonObject) element, materials);
					BoxBone bone = new BoxBone(boneName);
					bone.setModel(model);
					result.addBone(bone);
				}
			}
			
			
			
			
			return result;
		} catch (SyntaxError e) {
			throw new IOException(e.getCompleteMessage(), e);
		}
	}
	
	public static Model unpackElement(JsonObject element, List<Material> materials) throws IOException {
		Model result = new Model();
		HashMap<Material, Mesh> meshes = new HashMap<>();
		JsonArray fromArray = element.get(JsonArray.class, "from");
		JsonArray toArray = element.get(JsonArray.class, "to");
		JsonObject facesObject = element.getObject("faces");
		if (fromArray==null || toArray==null || facesObject==null) {
			throw new IOException("Missing one of the following required elements for each 'element' object: 'from', 'to', 'faces'.");
		}
		if (fromArray.size()<3) throw new IOException("Need 3 dimensions for 'from' in an 'element'.");
		if (toArray.size()<3) throw new IOException("Need 3 dimensions for 'to' in an 'element'.");
		if (facesObject.isEmpty())  return result; //No faces means an empty model
		
		Vector3d from = new Vector3d(
				Double.valueOf(fromArray.getDouble(0, 0.0)/16.0),
				Double.valueOf(fromArray.getDouble(1, 0.0)/16.0),
				Double.valueOf(fromArray.getDouble(2, 0.0)/16.0)
				);
		
		Vector3d to = new Vector3d(
				Double.valueOf(toArray.getDouble(0, 0.0)/16.0),
				Double.valueOf(toArray.getDouble(1, 0.0)/16.0),
				Double.valueOf(toArray.getDouble(2, 0.0)/16.0)
				);
		//System.out.println("Building from="+from+"to="+to);
		
		Mesh dummy = new Mesh();
		//Z-
		{
			Vertex a = new Vertex(new Vector3d(from.x, from.y, from.z), new Vector2d(1, 0));
			a.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_ZMINUS);
			
			Vertex b = new Vertex(new Vector3d(from.x, to.y, from.z), new Vector2d(1, 1));
			b.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_ZMINUS);
			
			Vertex c = new Vertex(new Vector3d(to.x, to.y, from.z), new Vector2d(0, 1));
			c.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_ZMINUS);
			
			Vertex d = new Vertex(new Vector3d(to.x, from.y, from.z), new Vector2d(0, 0));
			d.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_ZMINUS);
			
			Face face = new Face(a, b, c, d);
			dummy.addFace(face);
		}
		//Z+
		{
			Vertex a = new Vertex(new Vector3d(to.x, to.y, to.z), new Vector2d(1, 1));
			a.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_ZPLUS);
			
			Vertex b = new Vertex(new Vector3d(from.x, to.y, to.z), new Vector2d(0, 1));
			b.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_ZPLUS);
			
			Vertex c = new Vertex(new Vector3d(from.x, from.y, to.z), new Vector2d(0, 0));
			c.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_ZPLUS);
			
			Vertex d = new Vertex(new Vector3d(to.x, from.y, to.z), new Vector2d(1, 0));
			d.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_ZPLUS);
			
			Face face = new Face(a, b, c, d);
			dummy.addFace(face);
		}
		//Y-
		{
			Vertex a = new Vertex(new Vector3d(to.x, from.y, to.z), new Vector2d(0, 0));
			a.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_YMINUS);
			
			Vertex b = new Vertex(new Vector3d(from.x, from.y, to.z), new Vector2d(1, 0));
			b.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_YMINUS);
			
			Vertex c = new Vertex(new Vector3d(from.x, from.y, from.z), new Vector2d(1, 1));
			c.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_YMINUS);
			
			Vertex d = new Vertex(new Vector3d(to.x, from.y, from.z), new Vector2d(0, 1));
			d.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_YMINUS);
			
			
			Face face = new Face(a, b, c, d);
			dummy.addFace(face);
		}
		//Y+
		{
			Vertex a = new Vertex(new Vector3d(from.x, to.y, from.z), new Vector2d(1, 0));
			a.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_YPLUS);
			
			Vertex b = new Vertex(new Vector3d(from.x, to.y, to.z), new Vector2d(1, 1));
			b.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_YPLUS);
			
			Vertex c = new Vertex(new Vector3d(to.x, to.y, to.z), new Vector2d(0, 1));
			c.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_YPLUS);
			
			Vertex d = new Vertex(new Vector3d(to.x, to.y, from.z), new Vector2d(0, 0));
			d.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_YPLUS);
			
			Face face = new Face(a, b, c, d);
			dummy.addFace(face);
		}
		//X-
		{
			Vertex a = new Vertex(new Vector3d(from.x, to.y, to.z), new Vector2d(1, 1));
			a.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_XMINUS);
			
			Vertex b = new Vertex(new Vector3d(from.x, to.y, from.z), new Vector2d(0, 1));
			b.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_XMINUS);
			
			Vertex c = new Vertex(new Vector3d(from.x, from.y, from.z), new Vector2d(0, 0));
			c.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_XMINUS);
			
			Vertex d = new Vertex(new Vector3d(from.x, from.y, to.z), new Vector2d(1, 0));
			d.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_XMINUS);
			
			Face face = new Face(a, b, c, d);
			dummy.addFace(face);
		}
		//X+
		{
			Vertex a = new Vertex(new Vector3d(to.x, from.y, from.z), new Vector2d(1, 0));
			a.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_XPLUS);
			
			Vertex b = new Vertex(new Vector3d(to.x, to.y, from.z), new Vector2d(1, 1));
			b.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_XPLUS);
			
			Vertex c = new Vertex(new Vector3d(to.x, to.y, to.z), new Vector2d(0, 1));
			c.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_XPLUS);
			
			Vertex d = new Vertex(new Vector3d(to.x, from.y, to.z), new Vector2d(0, 0));
			d.putMaterialAttribute(MaterialAttribute.NORMAL, PlatonicSolidMesher.VEC_XPLUS);
			
			Face face = new Face(a, b, c, d);
			dummy.addFace(face);
		}
		result.addMesh(dummy);
		
		return result;
	}
}
