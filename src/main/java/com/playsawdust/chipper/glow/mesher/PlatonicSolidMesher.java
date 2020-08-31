/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.mesher;

import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.playsawdust.chipper.glow.model.Face;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Vertex;

public class PlatonicSolidMesher {
	public static final Vector3dc VEC_ZMINUS = new Vector3d( 0,  0, -1);
	public static final Vector3dc VEC_ZPLUS  = new Vector3d( 0,  0,  1);
	public static final Vector3dc VEC_XMINUS = new Vector3d(-1,  0,  0);
	public static final Vector3dc VEC_XPLUS  = new Vector3d( 1,  0,  0);
	public static final Vector3dc VEC_YMINUS = new Vector3d( 0, -1,  0);
	public static final Vector3dc VEC_YPLUS  = new Vector3d( 0,  1,  0);
	
	public static Mesh meshCube(double x, double y, double z, double xSize, double ySize, double zSize) {
		Mesh result = new Mesh();
		
		//Z-
		{
			Vertex a = new Vertex(new Vector3d(x, y, z), new Vector2d(1, 0));
			a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
			
			Vertex b = new Vertex(new Vector3d(x, y+ySize, z), new Vector2d(1, 1));
			b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
			
			Vertex c = new Vertex(new Vector3d(x+xSize, y+ySize, z), new Vector2d(0, 1));
			c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
			
			Vertex d = new Vertex(new Vector3d(x+xSize, y, z), new Vector2d(0, 0));
			d.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
			
			Face face = new Face(a, b, c, d);
			result.addFace(face);
		}
		//Z+
		{
			Vertex a = new Vertex(new Vector3d(x+xSize, y+ySize, z+zSize), new Vector2d(1, 1));
			a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZPLUS);
			
			Vertex b = new Vertex(new Vector3d(x, y+ySize, z+zSize), new Vector2d(0, 1));
			b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZPLUS);
			
			Vertex c = new Vertex(new Vector3d(x, y, z+zSize), new Vector2d(0, 0));
			c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZPLUS);
			
			Vertex d = new Vertex(new Vector3d(x+xSize, y, z+zSize), new Vector2d(1, 0));
			d.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZPLUS);
			
			Face face = new Face(a, b, c, d);
			result.addFace(face);
		}
		//Y-
		{
			Vertex a = new Vertex(new Vector3d(x+xSize, y, z+zSize), new Vector2d(0, 0));
			a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YMINUS);
			
			Vertex b = new Vertex(new Vector3d(x, y, z+zSize), new Vector2d(1, 0));
			b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YMINUS);
			
			Vertex c = new Vertex(new Vector3d(x, y, z), new Vector2d(1, 1));
			c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YMINUS);
			
			Vertex d = new Vertex(new Vector3d(x+xSize, y, z), new Vector2d(0, 1));
			d.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YMINUS);
			
			
			Face face = new Face(a, b, c, d);
			result.addFace(face);
		}
		//Y+
		{
			Vertex a = new Vertex(new Vector3d(x, y+ySize, z), new Vector2d(1, 0));
			a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YPLUS);
			
			Vertex b = new Vertex(new Vector3d(x, y+ySize, z+zSize), new Vector2d(1, 1));
			b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YPLUS);
			
			Vertex c = new Vertex(new Vector3d(x+xSize, y+ySize, z+zSize), new Vector2d(0, 1));
			c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YPLUS);
			
			Vertex d = new Vertex(new Vector3d(x+xSize, y+ySize, z), new Vector2d(0, 0));
			d.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YPLUS);
			
			Face face = new Face(a, b, c, d);
			result.addFace(face);
		}
		//X-
		{
			Vertex a = new Vertex(new Vector3d(x, y+ySize, z+zSize), new Vector2d(1, 1));
			a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XMINUS);
			
			Vertex b = new Vertex(new Vector3d(x, y+ySize, z), new Vector2d(0, 1));
			b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XMINUS);
			
			Vertex c = new Vertex(new Vector3d(x, y, z), new Vector2d(0, 0));
			c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XMINUS);
			
			Vertex d = new Vertex(new Vector3d(x, y, z+zSize), new Vector2d(1, 0));
			d.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XMINUS);
			
			Face face = new Face(a, b, c, d);
			result.addFace(face);
		}
		//X+
		{
			Vertex a = new Vertex(new Vector3d(x+xSize, y, z), new Vector2d(1, 0));
			a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XPLUS);
			
			Vertex b = new Vertex(new Vector3d(x+xSize, y+ySize, z), new Vector2d(1, 1));
			b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XPLUS);
			
			Vertex c = new Vertex(new Vector3d(x+xSize, y+ySize, z+zSize), new Vector2d(0, 1));
			c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XPLUS);
			
			Vertex d = new Vertex(new Vector3d(x+xSize, y, z+zSize), new Vector2d(0, 0));
			d.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XPLUS);
			
			Face face = new Face(a, b, c, d);
			result.addFace(face);
		}
		
		return result;
	}
	
	
}
