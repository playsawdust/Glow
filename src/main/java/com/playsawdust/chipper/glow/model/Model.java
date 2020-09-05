/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

public class Model implements ModelSupplier, Iterable<Mesh> {
	private ArrayList<Mesh> meshes = new ArrayList<>();
	
	/** Create an empty Model with no Meshes */
	public Model() {}
	
	public Model(Mesh mesh) {
		meshes.add(mesh);
	}
	
	public void addMesh(Mesh mesh) {
		meshes.add(mesh);
	}
	
	public int getMeshCount() {
		return meshes.size();
	}
	
	public MeshSupplier getMesh(int index) {
		return meshes.get(index);
	}
	
	public Iterator<Mesh> iterator() {
		return this.meshes.iterator();
	}
	
	public boolean isEmpty() {
		for(MeshSupplier mesh : meshes) {
			if (!mesh.supplyMesh().isEmpty()) return false;
		}
		return true;
	}
	
	/** Add all the Meshes from other into this Model, merging them with existing Meshes if the Materials match */
	public void combineFrom(Model other) {
		HashMap<Material, Mesh> mergeTargets = new HashMap<>();
		for(MeshSupplier mesh : meshes) {
			mergeTargets.put(mesh.getMaterial(), mesh.supplyMesh()); //we don't care if an entry gets overwritten, and it's more expensive to check.
		}
		
		for(MeshSupplier otherMesh : other) {
			Mesh existing = mergeTargets.get(otherMesh.getMaterial());
			if (existing!=null) {
				existing.combineFrom(otherMesh.supplyMesh());
			} else {
				Mesh copy = otherMesh.supplyMesh().copy();
				mergeTargets.put(copy.getMaterial(), copy);
				meshes.add(copy);
			}
		}
	}
	
	/** Add the Mesh into this Model, merging it with an existing Mesh if the Materials match */
	public void combineFrom(Mesh other) {
		for(Mesh mesh : meshes) {
			if (mesh.getMaterial().equals(other.getMaterial())) {
					mesh.combineFrom(other);
				return;
			}
		}
		
		meshes.add(other.copy());
	}

	@Override
	public Iterator<MeshSupplier> supplyMeshes() {
		return new AbstractIterator<MeshSupplier>() {
			private int cur = 0;
			@Override
			protected MeshSupplier computeNext() {
				if (cur>=meshes.size()) return endOfData();
				Mesh mesh = meshes.get(cur);
				cur++;
				return mesh;
			}
			
		};
	}
}
