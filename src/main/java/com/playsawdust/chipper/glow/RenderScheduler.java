package com.playsawdust.chipper.glow;

import java.util.ArrayList;

import com.playsawdust.chipper.glow.model.ImmutableModel;
import com.playsawdust.chipper.glow.stage.RenderPass;

public class RenderScheduler {
	private ArrayList<RenderPass> passes = new ArrayList<>();
	
	/**
	 * Schedule some renderable object
	 * @param o
	 * @return true if the object was scheduled for render.
	 */
	public boolean schedule(Object o) {
		if (o instanceof ImmutableModel) {
			return schedule((ImmutableModel)o);
		}
		
		for(RenderPass pass : passes) {
			if (pass.canEnqueue(o)) {
				pass.enqueue(o);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean schedule(ImmutableModel m) {
		//TODO: Break the model up into submodels and enqueue them separately
		
		for(RenderPass pass : passes) {
			if (pass.canEnqueue(m)) {
				pass.enqueue(m);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 */
	public void render() {
		for(RenderPass pass : passes) {
			pass.apply();
		}
	}
}
