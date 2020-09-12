/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.gl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.playsawdust.chipper.glow.util.AbstractGPUResource;

public class BakedModel extends AbstractGPUResource implements Iterable<BakedMesh> {
	private ArrayList<BakedMesh> meshes = new ArrayList<>();
	
	public BakedModel(Collection<BakedMesh> meshes) {
		for(BakedMesh mesh : meshes) this.meshes.add(mesh);
	}
	
	@Override
	public void _free() {
		for(BakedMesh mesh : meshes) {
			mesh.free();
		}
		
		meshes.clear();
	}

	@Override
	public Iterator<BakedMesh> iterator() {
		return meshes.iterator();
	}
}
