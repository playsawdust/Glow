package com.playsawdust.chipper.glow.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class EditableMesh {
	
	private Material material;
	private ArrayList<Vertex> vertices = new ArrayList<>();
	private ArrayList<Edge> edges = new ArrayList<>();
	private ArrayList<Face> faces = new ArrayList<>();
	
	private transient Object cacheData = null;
	private transient Object cacheOwner = null;
	
	public Material getMaterial() { return material; }
	
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
		Edge edge = edges.remove(index);
		if (edge!=null) cleanupEdgeRemoval(edge);
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
	
	/**
	 * Check for side-effects of removing an edge which is no longer in the model.
	 * <ul>
	 *   <li>Remove all faces which relied on the edge
	 *   <li>Remove all vertices which no longer contribute to an edge or face
	 * </ul>
	 */
	protected void cleanupEdgeRemoval(Edge removedEdge) {
		ArrayList<Face> removedFaces = new ArrayList<>();
		for(Face f : faces) {
			if (f.edges.contains(removedEdge)) {
				removedFaces.add(f);
			}
		}
		for(Face f : removedFaces) {
			if (faces.contains(f)) {
				faces.remove(f);
				cleanupFaceRemoval(f); //Additional edges and vertices may need killing at this point. Won't explode because additional edges from this call will be orphaned.
			}
		}
		
		
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
		
		public Vertex(Vector3d pos) {
			this.pos = new Vector3d(pos);
			this.uv = new Vector2d(0, 0);
		}

		public Vertex(Vector3d pos, Vector2d uv) {
			this.pos = new Vector3d(pos);
			this.uv = new Vector2d(uv);
		}

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
		
		public Face() {} //degenerate face though
		
		public Face(Vertex a, Vertex b, Vertex c) {
			this.vertices.add(a);
			this.vertices.add(b);
			this.vertices.add(c);
			this.edges.add(new Edge(a, b));
			this.edges.add(new Edge(b, c));
			this.edges.add(new Edge(c, a));
		}

		/** Takes vertices and edges and makes sure that they match, pulling new edges from meshEdgeList if they exist,
		 * or adding them to meshEdgeList if not present. Important note here, edges are a nice cached fiction, but
		 * vertices are real ground truth, as the winding order also determines facing.
		 */
		public void cleanup(ArrayList<Edge> meshEdgeList) {
			ArrayList<Edge> oldEdges = edges;
			edges = new ArrayList<>();
			Vertex lastVertex = null;
			for(Vertex v : vertices) {
				if (lastVertex!=null) {
					
					//Grab our existing edge if it exists
					Edge e = getEdge(oldEdges, v, lastVertex);
					
					if (e==null) {
						//Not present in our local edge cache, so grab it from the mesh if present
						e = getEdge(meshEdgeList, v, lastVertex);
						
						if (e==null) {
							//Not in local cache *or* mesh, so invent a new one and record it in the mesh
							e = new Edge(v, lastVertex);
							meshEdgeList.add(e);
						}
					}
					edges.add(e);
				}
				
				lastVertex = v;
			}
		}
		
		private Edge getEdge(ArrayList<Edge> edges, Vertex a, Vertex b) {
			for(Edge e : edges) {
				if (a.equals(e.a) && b.equals(e.b)) return e;
				if (b.equals(e.a) && a.equals(e.b)) return e;
			}
			
			return null;
		}
	}
}
