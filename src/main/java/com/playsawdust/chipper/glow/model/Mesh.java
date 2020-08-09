package com.playsawdust.chipper.glow.model;

import java.util.ArrayList;
import java.util.Collections;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.google.common.collect.ImmutableList;

public class Mesh {
	
	private Material material;
	//private ArrayList<Vertex> vertices = new ArrayList<>();
	//private ArrayList<Edge> edges = new ArrayList<>();
	private ArrayList<Face> faces = new ArrayList<>();
	
	private transient Object cacheData = null;
	private transient Object cacheOwner = null;
	
	public Material getMaterial() { return material; }
	/*
	public int getVertexCount() {
		return vertices.size();
	}
	
	public Vertex getVertex(int index) {
		return vertices.get(index);
	}
	
	public void addVertex(Vector3dc pos, Vector2dc uv) {
		vertices.add(new Vertex(new Vector3d(pos), new Vector2d(uv)));
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
	}*/
	
	public int getFaceCount() {
		return faces.size();
	}
	
	public Face getFace(int index) {
		return faces.get(index);
	}
	
	public void addFace(Face face) {
		faces.add(face);
		//for(Vertex v : face.vertices()) {
		//	if (!vertices.contains(v)) vertices.add(v);
		//}
	}
	
	public void removeFace(int index) {
		Face face = faces.remove(index);
		if (face!=null) cleanupFaceRemoval(face);
	}
	
	public void removeFace(Face face) {
		boolean removed = faces.remove(face);
		if (removed) cleanupFaceRemoval(face);
	}
	
	public Iterable<Face> faces() {
		return Collections.unmodifiableList(faces);
	}
	
	/**
	 * Caches flattened vertex attribute data in a way that, if resubmitted to the same RenderPass, can facilitate fast, dynamic render.
	 * @param owner The RenderPass or other vertex attribute data consumer
	 * @param data Vertex attribute data for reuse
	 */
	public void cache(Object owner, Object data) {
		this.cacheOwner = owner;
		this.cacheData = data;
	}
	
	/**
	 * Grabs vertex attribute data previously cached for reuse, provided that it has not been invalidated by a previous draw call.
	 * @param owner The caller - a RenderPass or other vertex attribute data consumer
	 * @return Data cached from a previous flatten operation on this mesh
	 */
	@SuppressWarnings("unchecked")
	public <T> @Nullable T getCache(Object owner, Class<T> cacheDataClass) {
		if (cacheData==null) return null;
		
		if (cacheOwner!=null && owner==cacheOwner) {
			if (cacheDataClass.isAssignableFrom(cacheData.getClass())) {
				return (T) cacheData;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Invalidates cached flat vertex attribute data on this mesh.
	 */
	public void clearCache() {
		cacheOwner = null;
		cacheData = null;
	}
	
	//Deduplicates vertices, edges, and faces, and removes orphaned edges and vertices. Does any other consistency checks worth doing
	public void cleanup() {
		//TODO: Dedupe vertices first, so everything orphaned as a result just gets cleaned up here
		/*
		ArrayList<Vertex> removedVertices = new ArrayList<>();
		for(Vertex v : vertices) {
			if (isOrphaned(v)) {
				removedVertices.add(v);
			}
		}
		
		for(Vertex v : removedVertices) {
			vertices.remove(v);
		}*/
	}
	
	/** Check all edges and vertices of a face which is no longer in the model, and if they're orphaned, remove them */
	protected void cleanupFaceRemoval(Face removedFace) {
		//ImmutableList<Vertex> toCheck = ImmutableList.copyOf(removedFace.vertices);
		//for(Vertex v : toCheck) {
		//	if (isOrphaned(v)) vertices.remove(v);
		//}
	}
	
	/** Returns true if and only if the provided Vertex is not an endpoint of any Edge (and therefore also not part of a Face) in this Mesh */
	public boolean isOrphaned(Vertex v) {
		for(Face f : faces) {
			if (f.vertices.contains(v)) return false;
		}
		
		return true;
	}
	
	public void setMaterial(Material m) {
		this.material = m;
	}
	
	public static class Face {
		ArrayList<Vertex> vertices = new ArrayList<>(); //size must be at least 3, size must match edges.size, all vertices must be present in edges, and must be listed in counter-clockwise order to express facing.
		
		public Face() {} //degenerate face though
		
		public Face(Vertex a, Vertex b, Vertex c) {
			this.vertices.add(a);
			this.vertices.add(b);
			this.vertices.add(c);
		}

		public Iterable<Vertex> vertices() {
			return Collections.unmodifiableList(vertices);
		}
	}
}
