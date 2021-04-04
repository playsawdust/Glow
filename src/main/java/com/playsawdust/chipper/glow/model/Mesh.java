/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.joml.AABBd;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector4d;


public class Mesh implements MeshSupplier {
	
	private Material material = Material.BLANK;
	private ArrayList<Face> faces = new ArrayList<>();
	
	@Override
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
	public void combineFrom(Mesh other) {
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
	
	public AABBd getBounds() {
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double minZ = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		double maxZ = Double.MIN_VALUE;
		
		for(Face face : faces) {
			for(Vertex vertex : face) {
				Vector3dc pos = vertex.getMaterialAttribute(MaterialAttribute.POSITION);
				minX = Math.min(minX, pos.x());
				minY = Math.min(minY, pos.y());
				minZ = Math.min(minZ, pos.z());
				maxX = Math.max(maxX, pos.x());
				maxY = Math.max(maxY, pos.y());
				maxZ = Math.max(maxZ, pos.z());
			}
		}
		
		return new AABBd(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	public void transform(Matrix4dc matrix) {
		Set<Vertex> processed = new HashSet<Vertex>(faces.size()*3); //Meshes frequently contain duplicate vertices. Make sure they only get processed once!
		for(Face face : faces) {
			for(Vertex vertex : face) {
				if (!processed.contains(vertex)) {
					Vector3dc pos = vertex.getMaterialAttribute(MaterialAttribute.POSITION);
					Vector4d transformed = matrix.transform(new Vector4d(pos, 1));
					//TODO: Scale the result so that W is 1. Not needed for affine transforms like rotations or translations, only for funky things like inverse projection
					vertex.putMaterialAttribute(MaterialAttribute.POSITION, new Vector3d(transformed.x, transformed.y, transformed.z));
					processed.add(vertex);
				}
			}
		}
	}
	
	public Mesh copy() {
		Mesh result = new Mesh();
		result.setMaterial(this.getMaterial());
		result.combineFrom(this);
		return result;
	}

	@Override
	public Mesh supplyMesh() {
		return this;
	}
}
