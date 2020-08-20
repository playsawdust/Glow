package com.playsawdust.chipper.glow.voxel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Vector3ic;

import com.playsawdust.chipper.glow.model.Material;

public class VoxelPatch {
	private int x = 0;
	private int y = 0;
	private int z = 0;
	private int xSize = 16;
	private int ySize = 16;
	private int zSize = 16;
	
	private int[] voxels = new int[16*16*16];
	private ArrayList<MeshableVoxel> palette = new ArrayList<>();
	
	public VoxelShape getShape(int x, int y, int z) {
		MeshableVoxel voxel = getVoxel(x, y, z);
		if (voxel==null) return VoxelShape.EMPTY;
		return voxel.getShape();
	}
	
	public Material getMaterial(int x, int y, int z) {
		MeshableVoxel voxel = getVoxel(x, y, z);
		if (voxel==null) return Material.GENERIC;
		return voxel.getMaterial();
	}
	
	/**
	 * Sets this patch's palette to a copy of the passed-in list.
	 * @param palette a List of MeshableVoxel objects where each element's position in the list is its ID in the voxel data of this patch
	 */
	public void setPalette(List<MeshableVoxel> palette) {
		this.palette.clear();
		for(int i=0; i<palette.size(); i++) {
			this.palette.add(palette.get(i));
		}
	}
	
	/**
	 * Sets the numeric voxel data in this patch. Numbers will be interpreted as indices into the MeshableVoxel palette.
	 * @param data numeric voxel data. MUST be xSize*ySize*zSize length or the chunk data may be ordered strangely!
	 */
	public void setData(int[] data) {
		int copyLength = Math.min(data.length, voxels.length);
		System.arraycopy(data, 0, voxels, 0, copyLength);
	}
	
	/**
	 * Sets the dimensions of this patch. Any existing data may be in a strange order after this!
	 */
	public void setSize(int xSize, int ySize, int zSize) {
		voxels = Arrays.copyOf(voxels, xSize*ySize*zSize);
		this.xSize = xSize;
		this.ySize = ySize;
		this.zSize = zSize;
	}
	
	public void setOffset(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public @Nullable MeshableVoxel getVoxel(int x, int y, int z) {
		int ofs = ofs(x, y, z);
		if (ofs==-1) return null;
		int data = voxels[ofs];
		return palette(data);
	}
	
	public void setVoxel(int x, int y, int z, MeshableVoxel voxel, boolean addToPalette) {
		int ofs = ofs(x, y, z);
		if (ofs==-1) return;
		int id = idFor(voxel);
		if (id==-1) {
			if (addToPalette) {
				id = palette.size();
				palette.add(voxel);
				voxels[ofs] = id;
			}
		} else {
			voxels[ofs] = id;
		}
	}
	
	private int ofs(int x, int y, int z) {
		if (x<this.x || y<this.x || z<this.z) return -1;
		if (x-this.x>=xSize || y-this.y>=ySize || z-this.z>=zSize) return -1;
		
		return (y-this.y) + ((x-this.x)*ySize) + ((z-this.z)*ySize*xSize);
	}
	
	private @Nullable MeshableVoxel palette(int id) {
		if (id>0 && id<palette.size()) return palette.get(id);
		return null;
	}
	
	private int idFor(MeshableVoxel voxel) {
		for(int i=0; i<palette.size(); i++) {
			if (palette.get(i).equals(voxel)) return i;
		}
		return -1;
	}
}
