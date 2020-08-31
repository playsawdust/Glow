/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
