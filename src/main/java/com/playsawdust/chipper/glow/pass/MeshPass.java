package com.playsawdust.chipper.glow.pass;

import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;

public class MeshPass implements RenderPass {

	@Override
	public void apply() {
	}

	@Override
	public boolean canEnqueue(Object o) {
		return (o instanceof Model) || (o instanceof Mesh); //&& matchesOtherOptions, e.g. shader, opacity
	}

	@Override
	public void enqueue(Object o) {
		//TODO: Implement
	}

}
