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
import java.util.function.Consumer;
import java.util.function.Function;

import org.joml.Vector3d;

import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.voxel.MeshableVoxel;
import com.playsawdust.chipper.glow.voxel.VoxelPatch;
import com.playsawdust.chipper.glow.voxel.VoxelShape;

public interface VoxelLoader {
	public static final MeshableVoxel VOXEL_EMPTY = new MeshableVoxel.SimpleMeshableVoxel().setShape(VoxelShape.EMPTY);
	
	/**
	 * Verifies that this data matches the format of this Loader and load it.
	 * <ul>
	 *   <li>If the file cannot be verified as a match for this Loader, null is returned. (e.g. the Loader loads MagicaVoxel .vox files, but the first byte of the file is anything except the ascii character 'V')
	 *   <li>If the file type is detected to match this Loader but is malformed, an IOException is thrown.
	 *   <li>If the file type matches and the data is loaded successfully, a VoxelPatch is returned representing the file contents.
	 * </ul>
	 * 
	 * <p>VoxelLoaders MUST be threadsafe.
	 * @param in an InputStream containing the data to be read.
	 * @param colorToVoxel a map function turning an int ARGB color into a MeshableVoxel.
	 * @param progressConsumer an object which will receive progress reports, in whole percents. 0 means the operation is starting, 100 means the operation is about to complete.
	 * @return the VoxelPatch represented by this InputStream
	 */
	public VoxelPatch tryVoxelLoad(InputStream in, Function<Integer, MeshableVoxel> colorToVoxel, Consumer<Integer> progressConsumer) throws IOException;
	
	
	public default VoxelPatch tryVoxelLoad(InputStream in, Consumer<Integer> progressConsumer) throws IOException {
		return this.tryVoxelLoad(in,
			(Integer col)->{
				if ((col & 0xFF000000) == 0) {
					return VOXEL_EMPTY;
				} else {
					int r = (col >> 16) & 0xFF;
					int g = (col >>  8) & 0xFF;
					int b = (col      ) & 0xFF;
					
					Vector3d colorVector = new Vector3d(r / 255.0, g / 255.0, b / 255.0);
					
					Material colorMaterial = new Material.Generic()
							.with(MaterialAttribute.DIFFUSE_COLOR, colorVector)
							.with(MaterialAttribute.DIFFUSE_TEXTURE_ID, "none")
							.with(MaterialAttribute.SPECULARITY, 0.4)
							.with(MaterialAttribute.EMISSIVITY, 0.0);
					return new MeshableVoxel.SimpleMeshableVoxel()
							.setShape(VoxelShape.CUBE)
							.setMaterial(colorMaterial);
				}
			}, progressConsumer);
	}
	
	public default VoxelPatch tryVoxelLoad(InputStream in) throws IOException {
		return tryVoxelLoad(in, (it)->{});
	}
}
