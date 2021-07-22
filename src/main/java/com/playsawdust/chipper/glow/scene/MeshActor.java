/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.scene;

import org.joml.Vector3dc;

import com.playsawdust.chipper.glow.gl.BakedModel;
import com.playsawdust.chipper.glow.model.Face;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;
import com.playsawdust.chipper.glow.model.Vertex;

public class MeshActor extends Actor {
	protected Mesh detailedCollision;
	//protected BakedModel renderObject;
	
	public MeshActor() {}
	
	public MeshActor(Model model, BakedModel baked) {
		detailedCollision = new Mesh();
		for(Mesh mesh : model) {
			detailedCollision.combineFrom(mesh);
		}
		setCollisionMesh(detailedCollision); //generates sphere volume
		
		this.renderObject = baked;
	}
	
	public void setRenderModel(BakedModel renderModel) {
		this.renderObject = renderModel;
	}
	
	public void setCollisionMesh(Mesh collisionMesh) {
		this.detailedCollision = collisionMesh;
		
		//Figure out the bounding sphere
		double furthestD2 = 0.0;
		for(Face face : collisionMesh.faces()) {
			for(Vertex v : face) {
				Vector3dc pos = v.getMaterialAttribute(MaterialAttribute.POSITION);
				double d = pos.distanceSquared(0, 0, 0);
				if (d>furthestD2) furthestD2 = d;
			}
		}
		
		this.collisionVolume = new CollisionVolume.Sphere(0,0,0, Math.sqrt(furthestD2));
	}
	
	public void setCollisionModel(Model collisionModel) {
		detailedCollision = new Mesh();
		for(Mesh mesh : collisionModel) {
			detailedCollision.combineFrom(mesh);
		}
		setCollisionMesh(detailedCollision);
	}
}
