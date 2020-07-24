package com.playsawdust.chipper.glow.model;

import org.joml.Vector2dc;
import org.joml.Vector3dc;
import org.joml.Vector4ic;

public interface ImmutableModel {
	public int getVertexCount();
	public Vector3dc getVertexPosition(int vert);
	public Vector4ic getVertexColor(int vert);
	public Vector2dc getTexturePositon(int vert);
}
