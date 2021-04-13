package com.playsawdust.chipper.glow.model.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import org.joml.AABBd;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import com.playsawdust.chipper.glow.mesher.VoxelMesher;
import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Model;
import com.playsawdust.chipper.glow.voxel.MeshableVoxel;
import com.playsawdust.chipper.glow.voxel.VoxelPatch;
import com.playsawdust.chipper.glow.voxel.VoxelShape;

public class QBLoader implements ModelLoader, VoxelLoader {
	private static final int VERSION_CURRENT = 
			(1 <<  0) | //MAJOR
			(1 <<  8) | //MINOR
			(0 << 16) | //RELEASE
			(0 << 24);  //BUILD
	
	private static final int VERSION_MAJOR_MASK = 0xFF;
	
	private static final MeshableVoxel VOXEL_EMPTY = new MeshableVoxel.SimpleMeshableVoxel().setShape(VoxelShape.EMPTY);
	
	@Override
	public Model tryModelLoad(InputStream in, Consumer<Integer> progressConsumer) throws IOException {
		VoxelPatch patch = tryVoxelLoad(in, (Integer col)->{
			if ((col & 0xFF000000) == 0) {
				return VOXEL_EMPTY;
			} else {
				Material colorMaterial = new Material.Generic()
						.with(MaterialAttribute.DIFFUSE_COLOR, colorVector(col))
						.with(MaterialAttribute.DIFFUSE_TEXTURE_ID, "none")
						.with(MaterialAttribute.SPECULARITY, 0.4)
						.with(MaterialAttribute.EMISSIVITY, 0.0);
				return new MeshableVoxel.SimpleMeshableVoxel()
						.setShape(VoxelShape.CUBE)
						.setMaterial(colorMaterial);
			}
		}, progressConsumer);
		
		Model result = VoxelMesher.mesh(0, 0, 0, patch.xSize(), patch.ySize(), patch.zSize(), patch::getShape, patch::getMaterial, 0.05);
		
		//Recenter model
		Vector3d translate = result.getCenter().mul(-1);
		Matrix4d op = new Matrix4d().translate(translate);
		result.transform(op);
		
		return result;
	}
	
	private static int readInt32(InputStream in) throws IOException {
		return
			 (in.read() & 0xFF)         |
			((in.read() & 0xFF) <<  8 ) |
			((in.read() & 0xFF) << 16 ) |
			((in.read() & 0xFF) << 24 );
	}
	
	private static Vector3d colorVector(int col) {
		int r = (col >> 16) & 0xFF;
		int g = (col >>  8) & 0xFF;
		int b = (col      ) & 0xFF;
		
		return new Vector3d(r / 255.0, g / 255.0, b / 255.0);
	}

	@Override
	public VoxelPatch tryVoxelLoad(InputStream in, Function<Integer, MeshableVoxel> colorToVoxel, Consumer<Integer> progressConsumer) throws IOException {
		// Header
		int version = readInt32(in);
		//System.out.println("Version: "+version+((version==VERSION_CURRENT)?" (current)":""));
		if ((version & VERSION_MAJOR_MASK) != (VERSION_CURRENT & VERSION_MAJOR_MASK)) throw new IOException("Version mismatch");
		
		int colorFormat = readInt32(in);
		if (colorFormat==0) {
			//System.out.println("Color Format: RGBA");
		} else if (colorFormat==1) {
			//System.out.println("ColorFormat: BGRA");
		} else {
			//System.out.println("Color Format: Unknown");
		}
		
		int orientation = readInt32(in);
		//System.out.println("Z-Axis Orientation: "+((orientation==0) ? "Left-handed" : "Right-handed"));
		
		boolean compression = readInt32(in)==1;
		//System.out.println("Compressed: "+compression);
		
		boolean visibilityMask = readInt32(in)==1;
		//System.out.println("Visibility Mask Encoded: "+visibilityMask);
		
		int matrixCount = readInt32(in);
		//System.out.println("Matrix Count: "+matrixCount);
		
		Model result = new Model();
		
		for(int i=0; i<matrixCount; i++) {
			int len = in.read() & 0xFF;
			byte[] stringBytes = new byte[len];
			for(int j=0; j<len; j++) stringBytes[j] = (byte)(in.read() & 0xFF);
			String matrixName = new String(stringBytes, StandardCharsets.US_ASCII);
			//System.out.println("Matrix Name: "+matrixName);
			int xsize = readInt32(in);
			int ysize = readInt32(in);
			int zsize = readInt32(in);
			
			int xpos = readInt32(in);
			int ypos = readInt32(in);
			int zpos = readInt32(in);
			
			VoxelPatch patch = new VoxelPatch(xsize, ysize, zsize);
			HashMap<Integer, MeshableVoxel> colorMaterials = new HashMap<>();
			patch.setVoxel(0, 0, 0, VOXEL_EMPTY, true);
			
			for(int z=0; z<zsize; z++) {
				for(int y=0; y<ysize; y++) {
					for(int x=0; x<xsize; x++) {
						int col = readInt32(in);
						if ((col & 0xFF000000) != 0) {
							int key = col & 0xFFFFFF;
							MeshableVoxel voxel = colorMaterials.get(key);
							if (voxel==null) {
								Material colorMaterial = new Material.Generic()
									.with(MaterialAttribute.DIFFUSE_COLOR, colorVector(col))
									.with(MaterialAttribute.DIFFUSE_TEXTURE_ID, "none")
									.with(MaterialAttribute.SPECULARITY, 0.4)
									.with(MaterialAttribute.EMISSIVITY, 0.0);
								voxel = new MeshableVoxel.SimpleMeshableVoxel()
										.setShape(VoxelShape.CUBE)
										.setMaterial(colorMaterial);
								colorMaterials.put(key, voxel);
								patch.setVoxel(x,y,z, voxel, true);
							} else {
								patch.setVoxel(x,y,z, voxel, false);
							}
							
						} else {
							patch.setVoxel(x, y, z, VOXEL_EMPTY, false); //Should be a NO-OP but let's make sure
						}
					}
				}
			}
			
			return patch;
		}
		return null; //TODO: If there are multiple models, adjust offsets and combine the patches.
	}
}
