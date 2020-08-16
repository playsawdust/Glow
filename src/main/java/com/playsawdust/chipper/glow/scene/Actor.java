package com.playsawdust.chipper.glow.scene;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3dc;

public interface Actor {
	/**
	 * Gets the position of this object in the scene
	 * @return the position of this object in the Scene
	 */
	public Vector3dc getPosition();
	public void getOrientation(Matrix3f matrix);
	
	public @Nullable Object getRenderObject();
	
	
	public void setPosition(Vector3dc position);
}
