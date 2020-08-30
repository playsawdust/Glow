package com.playsawdust.chipper.glow.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Model implements Iterable<Mesh> {
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
	
	public Mesh getMesh(int index) {
		return meshes.get(index);
	}
	
	public Iterator<Mesh> iterator() {
		return this.meshes.iterator();
	}
	
	public boolean isEmpty() {
		for(Mesh mesh : meshes) {
			if (!mesh.isEmpty()) return false;
		}
		return true;
	}
	
	/** Add all the Meshes from other into this Model, merging them with existing Meshes if the Materials match */
	public void combineFrom(Model other) {
		HashMap<Material, Mesh> mergeTargets = new HashMap<>();
		for(Mesh mesh : meshes) {
			mergeTargets.put(mesh.getMaterial(), mesh); //we don't care if an entry gets overwritten, and it's more expensive to check.
		}
		
		for(Mesh otherMesh : other) {
			Mesh existing = mergeTargets.get(otherMesh.getMaterial());
			if (existing!=null) {
				existing.combineFrom(otherMesh);
			} else {
				Mesh copy = otherMesh.copy();
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
			}
		}
		
		meshes.add(other.copy());
	}
}
