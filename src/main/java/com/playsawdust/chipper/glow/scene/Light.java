package com.playsawdust.chipper.glow.scene;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Light implements Actor {
	private Vector3d position = new Vector3d(0, 0, 0);
	private Vector3d direction = new Vector3d(0, 0, 0);
	private Vector3d color = new Vector3d(1, 1, 1);
	private double radius = 16.0;
	private double angle = 2.0;
	private double intensity = 1.0;

	@Override
	public Vector3dc getPosition() {
		return position;
	}
	
	public Vector3dc getDirection() {
		return direction;
	}
	
	@Override
	public void getOrientation(Matrix3f matrix) {
		matrix.identity(); //TODO: Generate a matrix from the look-vec
	}
	
	public Vector3dc getColor() {
		return color;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public double getIntensity() {
		return intensity;
	}
	
	public double getAngle() {
		return angle;
	}

	@Override
	public void setPosition(Vector3dc position) {
		this.position.set(position);
	}
	
	public void setPosition(double x, double y, double z) {
		this.position.set(x, y, z);
	}

	public void setColor(Vector3dc color) {
		this.color.set(color);
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}

	@Override
	public @Nullable Object getRenderObject() {
		return null;
	}
}
