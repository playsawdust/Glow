package com.playsawdust.chipper.glow.scene;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.playsawdust.chipper.glow.gl.BakedMesh;
import com.playsawdust.chipper.glow.model.Mesh;

public class MeshActor extends AbstractActor {
	protected Mesh detailedCollision;
	protected BakedMesh renderObject;
	
	@Override
	public @Nullable Object getRenderObject() {
		return renderObject;
	}

}
