package com.playsawdust.chipper.glow.voxel;

import com.playsawdust.chipper.glow.model.Material;

public interface MeshableVoxel {
	public Material getMaterial();
	public VoxelShape getShape();
	
	
	
	
	public static class SimpleMeshableVoxel implements MeshableVoxel {
		private Material material;
		private VoxelShape shape;
		
		@Override
		public Material getMaterial() {
			return this.material;
		}

		@Override
		public VoxelShape getShape() {
			return this.shape;
		}
		
		public SimpleMeshableVoxel setMaterial(Material material) {
			this.material = material;
			return this;
		}
		
		public SimpleMeshableVoxel setShape(VoxelShape shape) {
			this.shape = shape;
			return this;
		}
	}
}
