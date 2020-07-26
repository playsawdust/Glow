package com.playsawdust.chipper.glow.model;

import java.util.ArrayList;

import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class EditableModel implements ImmutableModel {
	
	private ArrayList<Vertex> vertices = new ArrayList<>();
	private ArrayList<Edge> edges = new ArrayList<>();
	private ArrayList<Face> faces = new ArrayList<>();
	
	@Override
	public int getVertexCount() {
		return vertices.size();
	}

	@Override
	public Vector3dc getVertexPosition(int vert) {
		if (vert<0 || vert>=vertices.size()) throw new ArrayIndexOutOfBoundsException();
		return vertices.get(vert).pos;
	}
	
	@Override
	public Vector2dc getTexturePosition(int vert) {
		if (vert<0 || vert>=vertices.size()) throw new ArrayIndexOutOfBoundsException();
		return vertices.get(vert).uv;
	}

	@Override
	public <T> T getVertexAttribute(int vert, MaterialAttribute<T> attrib) {
		if (vert<0 || vert>=vertices.size()) throw new ArrayIndexOutOfBoundsException();
		return vertices.get(vert).getMaterialAttribute(attrib);
	}
	
	public Vertex getVertex(int vert) {
		return vertices.get(vert);
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
	
	//Deduplicates vertices, edges, and faces, and removes orphaned edges and vertices. Does any other consistency checks worth doing
	public void cleanup() {
		//TODO: Implement
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
		
		public Vertex getA() { return a; }
		public Vertex getB() { return b; }
	}
	
	public static class Face {
		ArrayList<Edge> edges = new ArrayList<>(); //size must be at least 3. This is mostly for bookkeeping.
		ArrayList<Vertex> vertices = new ArrayList<>(); //size must be at least 3, size must match edges.size, all vertices must be present in edges, and must be listed in counter-clockwise order to express facing.
	}
}
