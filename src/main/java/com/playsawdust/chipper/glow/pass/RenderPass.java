package com.playsawdust.chipper.glow.pass;

import org.joml.Matrix3dc;
import org.joml.Matrix4dc;
import org.joml.Vector3dc;

import com.playsawdust.chipper.glow.RenderScheduler;
import com.playsawdust.chipper.glow.gl.BakedMesh;
import com.playsawdust.chipper.glow.gl.shader.Destroyable;
import com.playsawdust.chipper.glow.model.MaterialAttributeContainer;
import com.playsawdust.chipper.glow.model.Mesh;

public interface RenderPass extends Destroyable {
	public String getId();
	
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
	public void enqueue(Object o, Vector3dc position, Matrix3dc orientation, MaterialAttributeContainer environment);
	
	/**
	 * Activates the PipelineState for this pass, renders all enqueued objects, and then clears the queue. The PipelineState will remain active until set again by some other method.
	 */
	public void apply(Matrix4dc viewMatrix, RenderScheduler scheduler);
	
	/**
	 * If this RenderPass returns true for canEnqueue with Meshe objects, return a BakedMesh that can be scheduled on this pass. Otherwise the behavior of this method is undefined.
	 * @param mesh The Mesh to flatten and upload
	 */
	public BakedMesh bake(Mesh mesh);
}
