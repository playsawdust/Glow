package com.playsawdust.chipper.glow.model;

import java.util.ArrayList;

public class Model {
	private ArrayList<Mesh> meshes = new ArrayList<>();

	public void addMesh(Mesh mesh) {
		meshes.add(mesh);
	}
	
	public int getMeshCount() {
		return meshes.size();
	}
	
	public Mesh getMesh(int index) {
		return meshes.get(index);
	}
	
	public Iterable<Mesh> meshes() {
		return this.meshes;
	}
}
