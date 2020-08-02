package com.playsawdust.chipper.glow.pass;

import org.joml.Vector3dc;

public class PostProcessPass implements RenderPass {

	@Override
	public void apply() {
		
	}

	@Override
	public boolean canEnqueue(Object o) {
		return false;
	}

	@Override
	public void enqueue(Object o, Vector3dc position) {
		throw new UnsupportedOperationException();
	}

}
