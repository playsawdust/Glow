/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.mesher;

import java.util.HashMap;

import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.playsawdust.chipper.glow.model.Face;
import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;
import com.playsawdust.chipper.glow.model.Vertex;
import com.playsawdust.chipper.glow.voxel.VecFunction;
import com.playsawdust.chipper.glow.voxel.VoxelShape;

public class VoxelMesher {
	public static final Vector3dc VEC_ZMINUS = new Vector3d( 0,  0, -1);
	public static final Vector3dc VEC_ZPLUS  = new Vector3d( 0,  0,  1);
	public static final Vector3dc VEC_XMINUS = new Vector3d(-1,  0,  0);
	public static final Vector3dc VEC_XPLUS  = new Vector3d( 1,  0,  0);
	public static final Vector3dc VEC_YMINUS = new Vector3d( 0, -1,  0);
	public static final Vector3dc VEC_YPLUS  = new Vector3d( 0,  1,  0);
	
	public static Model mesh(
			int x1, int y1, int z1, int xSize, int ySize, int zSize,
			VecFunction<VoxelShape> getShape,
			VecFunction<Material> getMaterial) {
		return mesh(x1, y1, z1, xSize, ySize, zSize, getShape, getMaterial, 1.0);
	}
	
	public static Model mesh(
			int x1, int y1, int z1, int xSize, int ySize, int zSize,
			VecFunction<VoxelShape> getShape,
			VecFunction<Material> getMaterial,
			double voxelSize
			) {
		
		HashMap<Material, Mesh> meshes = new HashMap<>();
		
		for(int y=0; y<ySize; y++) {
			for(int z=0; z<zSize; z++) {
				for(int x=0; x<xSize; x++) {
					VoxelShape shape = getShape.apply(x, y, z);
					if (shape==VoxelShape.EMPTY) continue;
					
					Material material = getMaterial.apply(x, y, z);
					Mesh mesh = meshes.get(material);
					if (mesh==null) {
						mesh = new Mesh();
						mesh.setMaterial(material);
						meshes.put(material, mesh);
					}
					
					//Z-
					VoxelShape zMinus = getShape.apply(x,y,z-1);
					if (shape==VoxelShape.CUBE && zMinus!=VoxelShape.CUBE) {
						Vertex a = new Vertex(new Vector3d(x*voxelSize, y*voxelSize, z*voxelSize), new Vector2d(voxelSize, 0));
						a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
						
						Vertex b = new Vertex(new Vector3d(x*voxelSize, (y+1)*voxelSize, z*voxelSize), new Vector2d(voxelSize, voxelSize));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
						
						Vertex c = new Vertex(new Vector3d((x+1)*voxelSize, (y+1)*voxelSize, z*voxelSize), new Vector2d(0, voxelSize));
						c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
						
						Vertex d = new Vertex(new Vector3d((x+1)*voxelSize, y*voxelSize, z*voxelSize), new Vector2d(0, 0));
						d.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
						
						Face face = new Face(a, b, c, d);
						mesh.addFace(face);
					}
					
					//Z+
					VoxelShape zPlus = getShape.apply(x,y,z+1);
					if (shape==VoxelShape.CUBE && zPlus!=VoxelShape.CUBE) {
						Vertex a = new Vertex(new Vector3d((x+1)*voxelSize, (y+1)*voxelSize, (z+1)*voxelSize), new Vector2d(voxelSize, voxelSize));
						a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZPLUS);
						
						Vertex b = new Vertex(new Vector3d(x*voxelSize, (y+1)*voxelSize, (z+1)*voxelSize), new Vector2d(0, voxelSize));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZPLUS);
						
						Vertex c = new Vertex(new Vector3d(x*voxelSize, y*voxelSize, (z+1)*voxelSize), new Vector2d(0, 0));
						c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZPLUS);
						
						Vertex d = new Vertex(new Vector3d((x+1)*voxelSize, y*voxelSize, (z+1)*voxelSize), new Vector2d(voxelSize, 0));
						d.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZPLUS);
						
						Face face = new Face(a, b, c, d);
						mesh.addFace(face);
					}
					
					//Y-
					VoxelShape yMinus = getShape.apply(x,y-1,z);
					if (shape==VoxelShape.CUBE && yMinus!=VoxelShape.CUBE) {
						Vertex a = new Vertex(new Vector3d((x+1)*voxelSize, y*voxelSize, (z+1)*voxelSize), new Vector2d(0, 0));
						a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YMINUS);
						
						Vertex b = new Vertex(new Vector3d(x*voxelSize, y*voxelSize, (z+1)*voxelSize), new Vector2d(voxelSize, 0));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YMINUS);
						
						Vertex c = new Vertex(new Vector3d(x*voxelSize, y*voxelSize, z*voxelSize), new Vector2d(voxelSize, voxelSize));
						c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YMINUS);
						
						Vertex d = new Vertex(new Vector3d((x+1)*voxelSize, y*voxelSize, z*voxelSize), new Vector2d(0, voxelSize));
						d.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YMINUS);
						
						
						Face face = new Face(a, b, c, d);
						mesh.addFace(face);
					}
					
					//Y+
					VoxelShape yPlus = getShape.apply(x,y+1,z);
					if (shape==VoxelShape.CUBE && yPlus!=VoxelShape.CUBE) {
						Vertex a = new Vertex(new Vector3d(x*voxelSize, (y+1)*voxelSize, z*voxelSize), new Vector2d(voxelSize, 0));
						a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YPLUS);
						
						Vertex b = new Vertex(new Vector3d(x*voxelSize, (y+1)*voxelSize, (z+1)*voxelSize), new Vector2d(voxelSize, voxelSize));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YPLUS);
						
						Vertex c = new Vertex(new Vector3d((x+1)*voxelSize, (y+1)*voxelSize, (z+1)*voxelSize), new Vector2d(0, voxelSize));
						c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YPLUS);
						
						Vertex d = new Vertex(new Vector3d((x+1)*voxelSize, (y+1)*voxelSize, z*voxelSize), new Vector2d(0, 0));
						d.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YPLUS);
						
						Face face = new Face(a, b, c, d);
						mesh.addFace(face);
					}
					
					//X-
					VoxelShape xMinus = getShape.apply(x-1,y,z);
					if (shape==VoxelShape.CUBE && xMinus!=VoxelShape.CUBE) {
						Vertex a = new Vertex(new Vector3d(x*voxelSize, (y+1)*voxelSize, (z+1)*voxelSize), new Vector2d(voxelSize, voxelSize));
						a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XMINUS);
						
						Vertex b = new Vertex(new Vector3d(x*voxelSize, (y+1)*voxelSize, z*voxelSize), new Vector2d(0, voxelSize));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XMINUS);
						
						Vertex c = new Vertex(new Vector3d(x*voxelSize, y*voxelSize, z*voxelSize), new Vector2d(0, 0));
						c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XMINUS);
						
						Vertex d = new Vertex(new Vector3d(x*voxelSize, y*voxelSize, (z+1)*voxelSize), new Vector2d(voxelSize, 0));
						d.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XMINUS);
						
						
						
						Face face = new Face(a, b, c, d);
						mesh.addFace(face);
					}
					
					//X+
					VoxelShape xPlus = getShape.apply(x+1,y,z);
					if (shape==VoxelShape.CUBE && xPlus!=VoxelShape.CUBE) {
						Vertex a = new Vertex(new Vector3d((x+1)*voxelSize, y*voxelSize, z*voxelSize), new Vector2d(voxelSize, 0));
						a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XPLUS);
						
						Vertex b = new Vertex(new Vector3d((x+1)*voxelSize, (y+1)*voxelSize, z*voxelSize), new Vector2d(voxelSize, voxelSize));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XPLUS);
						
						Vertex c = new Vertex(new Vector3d((x+1)*voxelSize, (y+1)*voxelSize, (z+1)*voxelSize), new Vector2d(0, voxelSize));
						c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XPLUS);
						
						Vertex d = new Vertex(new Vector3d((x+1)*voxelSize, y*voxelSize, (z+1)*voxelSize), new Vector2d(0, 0));
						d.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XPLUS);
						
						Face face = new Face(a, b, c, d);
						mesh.addFace(face);
					}
				}
			}
		}
		
		Model result = new Model();
		for(Mesh mesh : meshes.values()) {
			result.addMesh(mesh);
		}
		return result;
	}
	
	
}
