package com.playsawdust.chipper.glow.model;

import java.util.ArrayList;

public class Model {
	private ArrayList<EditableMesh> meshes = new ArrayList<>();

	public void addMesh(EditableMesh mesh) {
		meshes.add(mesh);
	}
	
	public int getMeshCount() {
		return meshes.size();
	}
	
	public EditableMesh getMesh(int index) {
		return meshes.get(index);
	}
	
	public Iterable<EditableMesh> meshes() {
		return this.meshes;
	}
}
