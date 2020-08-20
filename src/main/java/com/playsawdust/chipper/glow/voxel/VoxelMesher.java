package com.playsawdust.chipper.glow.voxel;

import java.util.HashMap;
import java.util.function.Function;

import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

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
	
	public Model mesh(
			int x1, int y1, int z1, int xSize, int ySize, int zSize,
			Function<Vector3ic, VoxelShape> getShape,
			Function<Vector3ic, Material> getMaterial
			) {
		
		HashMap<Material, Mesh> meshes = new HashMap<>();
		Vector3i cursor = new Vector3i();
		for(int y=0; y<ySize; y++) {
			for(int z=0; z<zSize; z++) {
				for(int x=0; x<xSize; x++) {
					cursor.set(x, y, z);
					VoxelShape shape = getShape.apply(cursor);
					if (shape==VoxelShape.EMPTY) continue;
					
					Material material = getMaterial.apply(cursor);
					Mesh mesh = meshes.get(material);
					if (mesh==null) {
						mesh = new Mesh();
						mesh.setMaterial(material);
						meshes.put(material, mesh);
					}
					
					//Z-
					cursor.set(x, y, z-1);
					VoxelShape zMinus = getShape.apply(cursor);
					if (shape==VoxelShape.CUBE && zMinus!=VoxelShape.CUBE) {
						Vertex a = new Vertex(new Vector3d(x, y, z), new Vector2d(0, 0));
						a.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
						
						Vertex b = new Vertex(new Vector3d(x, y+1, z), new Vector2d(0, 1));
						b.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
						
						Vertex c = new Vertex(new Vector3d(x+1, y+1, z), new Vector2d(1, 1));
						c.putMaterialAttribute(MaterialAttribute.NORMAL, VEC_ZMINUS);
						
						Mesh.Face face = new Mesh.Face(a, b, c);
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
