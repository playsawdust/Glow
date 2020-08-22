package com.playsawdust.chipper.glow.model;

import java.util.ArrayList;
import java.util.Collections;

import org.checkerframework.checker.nullness.qual.Nullable;


public class Mesh {
	
	private Material material = Material.GENERIC;
	private ArrayList<Face> faces = new ArrayList<>();
	
	private transient Object cacheData = null;
	private transient Object cacheOwner = null;
	
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
