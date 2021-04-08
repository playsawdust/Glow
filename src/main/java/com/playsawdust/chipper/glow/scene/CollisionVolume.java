/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.scene;

import javax.annotation.Nullable;

import org.joml.Vector3d;

//import org.joml.Vector3d;

public abstract class CollisionVolume {
	protected double sphereX = 0;
	protected double sphereY = 0;
	protected double sphereZ = 0;
	protected double sphereRadius = 0;
	
	public static class Sphere extends CollisionVolume {
		public Sphere(double x, double y, double z, double radius) {
			this.sphereX = x;
			this.sphereY = y;
			this.sphereZ = z;
			this.sphereRadius = radius;
		}
	}
	
	/**
	 * Gets the center of the collision sphere relative to the Actor. Not yet supported.
	 */
	public Vector3d getSphereOffset(@Nullable Vector3d dest) {
		if (dest==null) dest = new Vector3d();
		dest.set(sphereX, sphereY, sphereZ);
		return dest;
	}
	
	/**
	 * Gets the radius of the sphere representing the Actor's coarse collision area.
	 */
	public double getSphereRadius() {
		return sphereRadius;
	}
}
