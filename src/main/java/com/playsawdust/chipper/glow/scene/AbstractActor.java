package com.playsawdust.chipper.glow.scene;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public abstract class AbstractActor implements Actor {
	protected Vector3d position = new Vector3d();
	protected Matrix3d orientation = new Matrix3d();
	protected CollisionVolume collisionVolume = null;
	
	@Override
	public Vector3d getPosition(Vector3d result) {
		if (result==null) {
			return new Vector3d(position);
		} else {
			return result.set(position);
		}
	}
	
	@Override
	public void setPosition(Vector3dc position) {
		this.position.set(position);
	}
	
	@Override
	public Matrix3d getOrientation(Matrix3d result) {
		if (result==null) {
			return new Matrix3d(orientation);
		} else {
			return result.set(orientation);
		}
	}
	
	@Override
	public void setOrientation(Matrix3dc orientation) {
		this.orientation.set(orientation);
	}
	
	@Override
	public @Nullable CollisionVolume getCollision() {
		return collisionVolume;
	}
	
}
