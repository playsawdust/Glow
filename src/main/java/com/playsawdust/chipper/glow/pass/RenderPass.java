package com.playsawdust.chipper.glow.pass;

public interface RenderPass {
	
	/**
	 * Returns whether the entire object can be enqueued to render on this pass
	 * @param o the object to consider for enqueueing
	 * @return true if the *entire* object can be rendered by this pass. It is the responsibility of the scheduler, for example, to re-query with submodel parts if the whole model can't be rendered.
	 */
	public boolean canEnqueue(Object o);
	/**
	 * Enqueues the object for rendering. If canEnqueue returns false, or has not been called but would return false if called, then the behavior of this method is undefined.
	 * @param o the object to render
	 */
	public void enqueue(Object o);
	
	/**
	 * Activates the PipelineState for this pass, renders all enqueued objects, and then clears the queue. The PipelineState will remain active until set again by some other method.
	 */
	public void apply();
}
