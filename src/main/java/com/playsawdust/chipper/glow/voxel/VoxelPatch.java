package com.playsawdust.chipper.glow.voxel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.util.Histogram;

public class VoxelPatch {
	private int x = 0;
	private int y = 0;
	private int z = 0;
	private int xSize = 16;
	private int ySize = 16;
	private int zSize = 16;
	
	private int[] voxels = new int[16*16*16];
	private ArrayList<MeshableVoxel> palette = new ArrayList<>();
	
	private boolean lossless = true;
	
	public VoxelShape getShape(int x, int y, int z) {
		MeshableVoxel voxel = getVoxel(x, y, z);
		if (voxel==null) return VoxelShape.EMPTY;
		return voxel.getShape();
	}
	
	public Material getMaterial(int x, int y, int z) {
		MeshableVoxel voxel = getVoxel(x, y, z);
		if (voxel==null) return Material.BLANK;
		return voxel.getMaterial();
	}
	
	/** Returns a *mutable* palette used to interpret voxels in this patch. */
	public List<MeshableVoxel> getPalette() {
		return palette;
	}
	
	/** Gets a VoxelPatch half the size of this one which is a rough representation of it */
	public VoxelPatch getLoD() {
		if (xSize%2==1 || ySize%2==1 || zSize%2==1) return null;
		int halfX = xSize/2;
		int halfY = ySize/2;
		int halfZ = zSize/2;
		VoxelPatch result = new VoxelPatch();
		result.setSize(halfX, halfY, halfZ);
		
		for(MeshableVoxel v : palette) result.palette.add(v);
		
		for(int y=0; y<halfY; y++) {
			for(int z=0; z<halfZ; z++) {
				for(int x=0; x<halfX; x++) {
					//Grab 4 samples
					Histogram<Integer> histogram = new Histogram<>();
					
					histogram.add( getRaw(x*2,   y*2,   z*2  ) );
					histogram.add( getRaw(x*2+1, y*2,   z*2  ) );
					histogram.add( getRaw(x*2,   y*2+1, z*2  ) );
					histogram.add( getRaw(x*2,   y*2,   z*2+1) );
					histogram.add( getRaw(x*2+1, y*2+1, z*2  ) );
					histogram.add( getRaw(x*2+1, y*2,   z*2+1) );
					histogram.add( getRaw(x*2,   y*2+1, z*2+1) );
					histogram.add( getRaw(x*2+1, y*2+1, z*2+1) );
					
					if (histogram.size()>1) result.lossless = false;
					
					int best = histogram.getMode();
					int ofs = result.ofs(x,y,z);
					if (ofs==-1) continue; //Shouldn't happen
					result.voxels[ofs] = best;
				}
			}
		}
		
		return result;
	}
	
	public int xSize() { return xSize; }
	public int ySize() { return ySize; }
	public int zSize() { return zSize; }
	
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
	
	public int[] getData() {
		return voxels;
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
	
	public int getRaw(int x, int y, int z) {
		int ofs = ofs(x, y, z);
		if (ofs==-1) return 0;
		return voxels[ofs];
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
	
	public boolean isLossless() {
		return lossless;
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
