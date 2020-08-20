package com.playsawdust.chipper.glow.voxel;

import java.util.HashMap;

import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;
import com.playsawdust.chipper.glow.model.Vertex;

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
			VecFunction<Material> getMaterial
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
						Vertex a = new Vertex(new Vector3d(x, y, z), new Vector2d(1, 0));
						a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
						
						Vertex b = new Vertex(new Vector3d(x, y+1, z), new Vector2d(1, 1));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
						
						Vertex c = new Vertex(new Vector3d(x+1, y+1, z), new Vector2d(0, 1));
						c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
						
						Mesh.Face face = new Mesh.Face(a, b, c);
						mesh.addFace(face);
						
						b = new Vertex(new Vector3d(x+1, y, z), new Vector2d(0, 0));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
						face = new Mesh.Face(a, c, b);
						mesh.addFace(face);
					}
					
					//Z+
					VoxelShape zPlus = getShape.apply(x,y,z+1);
					if (shape==VoxelShape.CUBE && zPlus!=VoxelShape.CUBE) {
						Vertex a = new Vertex(new Vector3d(x, y, z+1), new Vector2d(0, 0));
						a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZPLUS);
						
						Vertex b = new Vertex(new Vector3d(x, y+1, z+1), new Vector2d(0, 1));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZPLUS);
						
						Vertex c = new Vertex(new Vector3d(x+1, y+1, z+1), new Vector2d(1, 1));
						c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZPLUS);
						
						Mesh.Face face = new Mesh.Face(a, c, b);
						mesh.addFace(face);
						
						b = new Vertex(new Vector3d(x+1, y, z+1), new Vector2d(1, 0));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZPLUS);
						face = new Mesh.Face(a, b, c);
						mesh.addFace(face);
					}
					
					//Y-
					VoxelShape yMinus = getShape.apply(x,y-1,z);
					if (shape==VoxelShape.CUBE && yMinus!=VoxelShape.CUBE) {
						Vertex a = new Vertex(new Vector3d(x, y, z), new Vector2d(1, 1));
						a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YMINUS);
						
						Vertex b = new Vertex(new Vector3d(x, y, z+1), new Vector2d(1, 0));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YMINUS);
						
						Vertex c = new Vertex(new Vector3d(x+1, y, z+1), new Vector2d(0, 0));
						c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YMINUS);
						
						Mesh.Face face = new Mesh.Face(a, c, b);
						mesh.addFace(face);
						
						b = new Vertex(new Vector3d(x+1, y, z), new Vector2d(0, 1));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YMINUS);
						face = new Mesh.Face(a, b, c);
						mesh.addFace(face);
					}
					
					//Y+
					VoxelShape yPlus = getShape.apply(x,y+1,z);
					if (shape==VoxelShape.CUBE && yPlus!=VoxelShape.CUBE) {
						Vertex a = new Vertex(new Vector3d(x, y+1, z), new Vector2d(1, 0));
						a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YPLUS);
						
						Vertex b = new Vertex(new Vector3d(x, y+1, z+1), new Vector2d(1, 1));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YPLUS);
						
						Vertex c = new Vertex(new Vector3d(x+1, y+1, z+1), new Vector2d(0, 1));
						c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YPLUS);
						
						Mesh.Face face = new Mesh.Face(a, b, c);
						mesh.addFace(face);
						
						b = new Vertex(new Vector3d(x+1, y+1, z), new Vector2d(0, 0));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_YPLUS);
						face = new Mesh.Face(a, c, b);
						mesh.addFace(face);
					}
					
					//X-
					VoxelShape xMinus = getShape.apply(x-1,y,z);
					if (shape==VoxelShape.CUBE && xMinus!=VoxelShape.CUBE) {
						Vertex a = new Vertex(new Vector3d(x, y, z), new Vector2d(0, 0));
						a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XMINUS);
						
						Vertex b = new Vertex(new Vector3d(x, y+1, z), new Vector2d(0, 1));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XMINUS);
						
						Vertex c = new Vertex(new Vector3d(x, y+1, z+1), new Vector2d(1, 1));
						c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XMINUS);
						
						Mesh.Face face = new Mesh.Face(a, c, b);
						mesh.addFace(face);
						
						b = new Vertex(new Vector3d(x, y, z+1), new Vector2d(1, 0));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XMINUS);
						face = new Mesh.Face(a, b, c);
						mesh.addFace(face);
					}
					
					//X+
					VoxelShape xPlus = getShape.apply(x+1,y,z);
					if (shape==VoxelShape.CUBE && xPlus!=VoxelShape.CUBE) {
						Vertex a = new Vertex(new Vector3d(x+1, y, z), new Vector2d(1, 0));
						a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XPLUS);
						
						Vertex b = new Vertex(new Vector3d(x+1, y+1, z), new Vector2d(1, 1));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XPLUS);
						
						Vertex c = new Vertex(new Vector3d(x+1, y+1, z+1), new Vector2d(0, 1));
						c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XPLUS);
						
						Mesh.Face face = new Mesh.Face(a, b, c);
						mesh.addFace(face);
						
						b = new Vertex(new Vector3d(x+1, y, z+1), new Vector2d(0, 0));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_XPLUS);
						face = new Mesh.Face(a, c, b);
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
