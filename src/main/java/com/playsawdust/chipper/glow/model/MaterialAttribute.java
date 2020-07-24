package com.playsawdust.chipper.glow.model;

/**
 * Represents some quality of a surface or a property at a vertex. For instance, UV location, diffuse color, or specularity.
 * Attributes could exist at either the vertex or material granularity. If a vertex value exists, and the pass supports per-vertex
 * values, vertices will override the material.
 * 
 * <p>The type parameter T is the immutable type for this attribute, returned from a direct query. For instance, a surface normal would be `Vec3di`.
 * <o>The type parameter U is the mutable type for this attribute, returned from a joml-style query. A surface normal would be `Vec3d`.
 */
public class MaterialAttribute<T, U> {
	private String name;
	
	public MaterialAttribute(String name) {
		this.name = name;
	}
	
	public String getName() { return name; }
	
	public String toString() {
		return "{ \"MaterialAttribute\": \""+name+"\" }";
	}
}
