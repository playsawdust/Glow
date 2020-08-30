package com.playsawdust.chipper.glow.model;

import java.util.ArrayList;
import java.util.Collections;


public class Mesh {
	
	private Material material = Material.BLANK;
	private ArrayList<Face> faces = new ArrayList<>();
	
	public Material getMaterial() { return material; }
	
	public int getFaceCount() {
		return faces.size();
	}
	
	public Face getFace(int index) {
		return faces.get(index);
	}
	
	public void addFace(Face face) {
		faces.add(face);
	}
	
	public void removeFace(int index) {
		faces.remove(index);
	}
	
	public void removeFace(Face face) {
		faces.remove(face);
	}
	
	public Iterable<Face> faces() {
		return Collections.unmodifiableList(faces);
	}
	
	/** Copies all face data from the {@code other} Mesh into this one. You can safely treat this as a deep copy since all Faces and Vertices are remade.
	 * 
	 * <p>The Material for the {@code other} Mesh is unused; all faces copied in will use this Mesh's Material. */
	public void combineWith(Mesh other) {
		for(Face face : other.faces) {
			this.faces.add(face.copy());
		}
	}
	
	/** Returns true if and only if the provided Vertex is not an endpoint of any Edge (and therefore also not part of a Face) in this Mesh */
	public boolean isOrphaned(Vertex v) {
		for(Face f : faces) {
			if (f.vertices.contains(v)) return false;
		}
		
		return true;
	}
	
	public boolean isEmpty() {
		return faces.isEmpty();
	}
	
	public void setMaterial(Material m) {
		this.material = m;
	}
}
