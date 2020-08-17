package com.playsawdust.chipper.glow.scene;

import org.joml.Vector3d;

public class CollisionVolume {
	public static class Capsule extends CollisionVolume {
		private Vector3d a = new Vector3d();
		public boolean collidesWith(CollisionVolume other) {
			if (other instanceof Capsule) {
				
			}
			
			return false;
		}
	}
}
