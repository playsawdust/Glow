package com.playsawdust.chipper.glow.gl.shader;

/**
 * This interface indicates that an object manages an offheap resource, such as malloc'd system memory
 * regions, or OpenGL objects residing on the GPU. {@link #destroy()} MUST be called before letting
 * the reference to this object be garbage collected.
 */
public interface Destroyable {
	/**
	 * Release any offheap or OpenGL resources held by this object.
	 * MUST be called before this object is allowed to garbage collect, but safe to call multiple times.
	 */
	public void destroy();
}
