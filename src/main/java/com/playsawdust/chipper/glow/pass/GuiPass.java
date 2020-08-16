package com.playsawdust.chipper.glow.pass;

import org.joml.Matrix3dc;
import org.joml.Vector3dc;

import com.playsawdust.chipper.glow.gl.BakedMesh;
import com.playsawdust.chipper.glow.model.Mesh;

public class GuiPass implements RenderPass {
	
	@Override
	public void apply() {
		
	}

	@Override
	public boolean canEnqueue(Object o) {
		return false;
	}

	@Override
	public void enqueue(Object o, Vector3dc position, Matrix3dc orientation) {
		//TODO: Implement
	}
	
	@Override
	public BakedMesh bake(Mesh mesh) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}
}
