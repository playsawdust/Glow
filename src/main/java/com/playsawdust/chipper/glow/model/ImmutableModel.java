package com.playsawdust.chipper.glow.model;

import org.joml.Vector2dc;
import org.joml.Vector3dc;

public interface ImmutableModel {
	public int getVertexCount();
	public Vector3dc getVertexPosition(int vert);
	public Vector2dc getTexturePosition(int vert);
	public <T> T getVertexAttribute(int vert, MaterialAttribute<T> attrib);
}
