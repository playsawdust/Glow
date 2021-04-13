/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.scene;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public abstract class Actor {
	protected Vector3d position = new Vector3d();
	protected Vector3d lastPosition = new Vector3d();
	protected Matrix3d orientation = new Matrix3d();
	protected Matrix3d lastOrientation = new Matrix3d();
	protected Vector3d velocity = new Vector3d();
	protected CollisionVolume collisionVolume = null;
	protected Object renderObject = null;
	
	protected boolean visible = true;
	protected boolean doPhysics = false;
	protected boolean doCollision = false;
	
	public Object getRenderObject(Camera camera) {
		return renderObject;
	}
	
	public void setRenderObject(Object obj) {
		renderObject = obj;
	}
	
	public Vector3d getPosition(Vector3d result) {
		if (result==null) {
			return new Vector3d(position);
		} else {
			return result.set(position);
		}
	}
	
	public void setPosition(Vector3dc position) {
		this.position.set(position);
	}
	
	public void setPosition(double x, double y, double z) {
		this.position.set(x, y, z);
	}
	
	public Matrix3d getOrientation(Matrix3d result) {
		if (result==null) {
			return new Matrix3d(orientation);
		} else {
			return result.set(orientation);
		}
	}
	
	public void setOrientation(Matrix3dc orientation) {
		this.orientation.set(orientation);
	}
	
	public void setOrientation(Vector3dc lookVec) {
		setOrientation(lookVec.x(), lookVec.y(), lookVec.z());
	}
	
	public void setOrientation(double x, double y, double z) {
		this.orientation.setLookAlong(x, y, z, 0, 1, 0);
	}
	
	public Matrix3d getLastOrientation(Matrix3d result) {
		if (result==null) result = new Matrix3d();
		return result.set(lastOrientation);
	}
	
	public void setLastOrientation(Matrix3dc orientation) {
		lastOrientation.set(orientation);
	}
	
	public Vector3d getLastPosition(Vector3d result) {
		if (result==null) result = new Vector3d();
		return result.set(lastPosition);
	}
	
	public void setLastPosition(Vector3dc position) {
		this.lastPosition.set(position);
	}
	
	/** Sets last position to current position */
	public void clearLastPosition() {
		this.lastPosition.set(this.position);
	}
	
	public Vector3d getVelocity(Vector3d result) {
		if (result==null) result = new Vector3d();
		return result.set(velocity);
	}
	
	public void setVelocity(Vector3dc velocity) {
		this.velocity.set(velocity);
	}
	
	public @Nullable CollisionVolume getCollision() {
		return collisionVolume;
	}
	
	public void lookAlong(double x, double y, double z) {
		//TODO: Can I turn this into axisAngles? I can!
		//double yRot = 0;
		double yRot = - Math.atan2(z, x) - Math.PI/2;
		double xRot = Math.atan2(y, Math.sqrt(x*x + z*z));
		//double xRot = 0;
		orientation
			.identity()
			
			.rotate(yRot, 0, 1, 0)
			.rotate(xRot, 1, 0, 0);
	}
	
	public void lookAt(double x, double y, double z) {
		//TODO: Can I turn this into axisAngles? I can!
		/*double yRot = Math.atan2(z, x);
		double xRot = Math.atan2(y, z);
		
		new Matrix3d()
			.identity();
			.rotate(xRot, 1, 0, 0);
			.rotate(yRot, 0, 1, 0);*/
		Vector3d lookVec = new Vector3d(x, y, z).sub(position).normalize();
		setOrientation(lookVec);
	}
}
