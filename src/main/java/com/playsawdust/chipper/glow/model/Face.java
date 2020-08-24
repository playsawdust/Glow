package com.playsawdust.chipper.glow.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Face implements Iterable<Vertex> {
	ArrayList<Vertex> vertices = new ArrayList<>(); //size must be at least 3, size must match edges.size, all vertices must be present in edges, and must be listed in counter-clockwise order to express facing.
	
	public Face() {} //degenerate face though
	
	public Face(Vertex a, Vertex b, Vertex c) {
		this.vertices.add(a);
		this.vertices.add(b);
		this.vertices.add(c);
	}
	
	public Face(Vertex a, Vertex b, Vertex c, Vertex d) {
		this.vertices.add(a);
		this.vertices.add(b);
		this.vertices.add(c);
		this.vertices.add(d);
	}
	
	/** @deprecated Face itself is now Iterable&lt;Vertex&gt; */
	@Deprecated
	public Iterable<Vertex> vertices() {
		return Collections.unmodifiableList(vertices);
	}
	
	public int vertexCount() {
		return vertices.size();
	}

	@Override
	public Iterator<Vertex> iterator() {
		return vertices.iterator();
	}
}