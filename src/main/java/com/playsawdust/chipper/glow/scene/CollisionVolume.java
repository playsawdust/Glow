/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.scene;

//import org.joml.Vector3d;

public class CollisionVolume {
	public static class Capsule extends CollisionVolume {
		//private Vector3d a = new Vector3d();
		public boolean collidesWith(CollisionVolume other) {
			if (other instanceof Capsule) {
				
			}
			
			return false;
		}
	}
}
