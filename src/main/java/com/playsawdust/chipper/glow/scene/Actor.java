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

public interface Actor { //TODO: Lerp position and rotation stuff
	/**
	 * Gets the position of this Actor in the Scene
	 * @param result a Vector to place the result into, or null to create a new Vector
	 * @return the position of this object in the Scene
	 */
	public Vector3d getPosition(Vector3d result);
	
	/**
	 * Gets a matrix representing the rotated orientation of this Actor.
	 * 
	 * <p>If you multiply any object-space vector by the matrix this function yields, it will
	 * rotate around the origin into the same orientation this Actor is rendered in.
	 * 
	 * @param result a Matrix to place the result into, or null to create a new Matrix
	 * @return a Matrix representing the rotation of this Actor
	 */
	public Matrix3d getOrientation(Matrix3d result);
	
	/**
	 * Gets an Object which can be understood by the RenderScheduler, such as a BakedModel,
	 * to represent this Actor in the Scene.
	 * @return An object for use with {@link com.playsawdust.chipper.glow.RenderScheduler#schedule(Object, Vector3dc, Matrix3dc, com.playsawdust.chipper.glow.model.MaterialAttributeContainer) RenderScheduler.schedule()}
	 */
	public @Nullable Object getRenderObject(Camera camera);
	
	/**
	 * Gets a CollisionVolume which represents a physics shape for this Actor, or null if this Actor cannot take part in collisions or physical interactions.
	 * @return a CollisionVolume which represents a physics shape for this Actor, or null if this Actor cannot take part in collisions or physical interactions.
	 */
	public @Nullable CollisionVolume getCollision();
	
	/**
	 * Unconditionally sets the position of this Actor in the Scene. The new location is not checked for collisions, and the Actor does not
	 * travel through any of the intervening space between its old position and the new one.
	 * @param position the new location in the Scene for this Actor.
	 */
	public void setPosition(Vector3dc position);
	
	/**
	 * Unconditionally sets the orientation of this Actor for rendering in the Scene. Depending on the Actor and CollisionShape, this may also affect its CollisionShape orientation.
	 * The Actor does not pass through any of the intervening orientations on the way to its new rotation, and the new orientation is not checked for collisions.
	 * @param orientation
	 */
	public void setOrientation(Matrix3dc orientation);
}
