package com.playsawdust.chipper.glow.model;

import java.util.ArrayList;

import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class EditableMesh {
	
	private Material material;
	private ArrayList<Vertex> vertices = new ArrayList<>();
	private ArrayList<Edge> edges = new ArrayList<>();
	private ArrayList<Face> faces = new ArrayList<>();
	
	public Material getMaterial() { return material; }
	
	public int getVertexCount() {
		return vertices.size();
	}
	
	public Vertex getVertex(int index) {
		return vertices.get(index);
	}
	
	public void addVertex(Vector3dc pos, Vector2dc uv) {
		Vertex toAdd = new Vertex();
		toAdd.pos = new Vector3d(pos);
		toAdd.uv = new Vector2d(uv);
		vertices.add(toAdd);
	}
	
	public void addVertex(Vertex v) {
		vertices.add(v);
	}
	
	public void removeVertex(int index) {
		vertices.remove(index);
		//TODO: Subset cleanup
	}
	
	public void removeVertex(Vertex v) {
		vertices.remove(v);
		//TODO: Subset cleanup
	}
	
	public int getEdgeCount() {
		return edges.size();
	}
	
	public Edge getEdge(int index) {
		return edges.get(index);
	}
	
	public void addEdge(Edge edge) {
		edges.add(edge);
	}
	
	public void addEdge(Vertex a, Vertex b) {
		edges.add(new Edge(a, b));
	}
	
	public void removeEdge(int index) {
		edges.remove(index);
		//TODO: Subset cleanup
	}
	
	public void removeEdge(Edge edge) {
		boolean removed = edges.remove(edge);
		if (removed) cleanupEdgeRemoval(edge);
	}
	
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
		Face face = faces.remove(index);
		if (face!=null) cleanupFaceRemoval(face);
	}
	
	public void removeFace(Face face) {
		boolean removed = faces.remove(face);
		if (removed) cleanupFaceRemoval(face);
	}
	
	//Deduplicates vertices, edges, and faces, and removes orphaned edges and vertices. Does any other consistency checks worth doing
	public void cleanup() {
		//TODO: Dedupe vertices first, so everything orphaned as a result just gets cleaned up
		
		ArrayList<Edge> removedEdges = new ArrayList<>();
		for(Edge e : edges) {
			if (isOrphaned(e)) {
				removedEdges.add(e);
			}
		}
		
		for(Edge e : removedEdges) {
			edges.remove(e);
		}
		
		ArrayList<Vertex> removedVertices = new ArrayList<>();
		for(Vertex v : vertices) {
			if (isOrphaned(v)) {
				removedVertices.add(v);
			}
		}
		
		for(Vertex v : removedVertices) {
			vertices.remove(v);
		}
	}
	
	/** Check all edges and vertices of a face which is no longer in the model, and if they're orphaned, remove them */
	protected void cleanupFaceRemoval(Face removedFace) {
		ArrayList<Edge> removedEdges = new ArrayList<>();
		for(Edge e : removedFace.edges) {
			if (isOrphaned(e)) {
				removedEdges.add(e);
				edges.remove(e);
			}
		}
		
		for(Edge e : removedEdges) {
			cleanupEdgeRemoval(e);
		}
	}
	
	/** Check all vertices of an edge which is no longer in the model, and if they're orphaned, remove them. */
	protected void cleanupEdgeRemoval(Edge removedEdge) {
		if (isOrphaned(removedEdge.a)) {
			vertices.remove(removedEdge.a);
		}
		
		if (isOrphaned(removedEdge.b)) {
			vertices.remove(removedEdge.b);
		}
	}
	
	/** Returns true if and only if the provided Edge is contained by no Face in the mesh. */
	public boolean isOrphaned(Edge e) {
		for(Face f : faces) {
			if (f.edges.contains(e)) return false;
		}
		
		return true;
	}
	
	/** Returns true if and only if the provided Vertex is not an endpoint of any Edge (and therefore also not part of a Face) in this Mesh */
	public boolean isOrphaned(Vertex v) {
		for(Edge e : edges) {
			if (e.a.equals(v) || e.b.equals(v)) return false;
		}
		
		return true;
	}
	
	public void setMaterial(Material m) {
		this.material = m;
	}
	
	public static class Vertex implements MaterialAttributeContainer, MaterialAttributeDelegateHolder {
		protected Vector3d pos;
		protected Vector2d uv;
		protected SimpleMaterialAttributeContainer attributes = new SimpleMaterialAttributeContainer();
		
		@Override
		public MaterialAttributeContainer getDelegate() {
			return attributes;
		}
		
	}
	
	/** Basically a tuple of two vertices, defines a polygon edge */
	public static class Edge {
		Vertex a;
		Vertex b;
		
		public Edge() {}
		public Edge(Vertex a, Vertex b) {
			this.a = a;
			this.b = b;
		}
		
		public Vertex getA() { return a; }
		public Vertex getB() { return b; }
	}
	
	public static class Face {
		ArrayList<Edge> edges = new ArrayList<>(); //size must be at least 3. This is mostly for bookkeeping.
		ArrayList<Vertex> vertices = new ArrayList<>(); //size must be at least 3, size must match edges.size, all vertices must be present in edges, and must be listed in counter-clockwise order to express facing.
	}
}
