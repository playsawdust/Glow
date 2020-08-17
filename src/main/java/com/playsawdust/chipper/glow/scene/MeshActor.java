package com.playsawdust.chipper.glow.scene;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.playsawdust.chipper.glow.gl.BakedMesh;
import com.playsawdust.chipper.glow.gl.shader.Destroyable;
import com.playsawdust.chipper.glow.model.Mesh;

public class MeshActor implements Actor {
	private Mesh mesh;
	private BakedMesh bakedMesh;
	private Vector3d position = new Vector3d();

	@Override
	public Vector3dc getPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getOrientation(Matrix3f matrix) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public @Nullable Object getRenderObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPosition(Vector3dc position) {
		// TODO Auto-generated method stub
		
	}
	
}
