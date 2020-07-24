package com.playsawdust.chipper.glow.stage;

public class PostProcessPass implements RenderPass {

	@Override
	public void apply() {
		
	}

	@Override
	public boolean canEnqueue(Object o) {
		return false;
	}

	@Override
	public void enqueue(Object o) {
		throw new UnsupportedOperationException();
	}

}
