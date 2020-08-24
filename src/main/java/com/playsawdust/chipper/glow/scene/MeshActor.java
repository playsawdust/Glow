package com.playsawdust.chipper.glow.scene;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.playsawdust.chipper.glow.gl.BakedModel;
import com.playsawdust.chipper.glow.model.Mesh;

public class MeshActor extends AbstractActor {
	protected Mesh detailedCollision;
	protected BakedModel renderObject;
	
	@Override
	public @Nullable Object getRenderObject() {
		return renderObject;
	}
	
	public void setRenderModel(BakedModel renderModel) {
		this.renderObject = renderModel;
	}
	
	public void setCollisionMesh(Mesh collisionMesh) {
		this.detailedCollision = collisionMesh;
	}
}
