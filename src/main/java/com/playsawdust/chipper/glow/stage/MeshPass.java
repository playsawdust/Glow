package com.playsawdust.chipper.glow.stage;

import com.playsawdust.chipper.glow.model.ImmutableModel;

public class MeshPass implements RenderPass {

	@Override
	public void apply() {
		
	}

	@Override
	public boolean canEnqueue(Object o) {
		return (o instanceof ImmutableModel); //&& matchesOtherOptions, e.g. shader, opacity
	}

	@Override
	public void enqueue(Object o) {
		//TODO: Implement
	}

}
