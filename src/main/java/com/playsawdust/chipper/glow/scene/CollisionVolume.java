/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.scene;

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
	 * If this collision volume is a sphere
	 * @param dest
	 * @return
	 */
	public Vector3d getSphereOffset(Vector3d dest) {
		dest.set(sphereX, sphereY, sphereZ);
		return dest;
	}
	
	public double getSphereRadius() {
		return sphereRadius;
	}
}
