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

import com.playsawdust.chipper.glow.gl.BakedModel;
import com.playsawdust.chipper.glow.model.Mesh;

public class MeshActor extends AbstractActor {
	protected Mesh detailedCollision;
	protected BakedModel renderObject;
	
	@Override
	public @Nullable Object getRenderObject(Camera camera) {
		return renderObject;
	}
	
	public void setRenderModel(BakedModel renderModel) {
		this.renderObject = renderModel;
	}
	
	public void setCollisionMesh(Mesh collisionMesh) {
		this.detailedCollision = collisionMesh;
	}
}
