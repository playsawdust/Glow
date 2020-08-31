/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.gl.shader;

/**
 * This interface indicates that an object manages an offheap resource, such as malloc'd system memory
 * regions, or OpenGL objects residing on the GPU. {@link #destroy()} MUST be called before letting
 * the reference to this object be garbage collected.
 * 
 * <p>This interface is also a strong sign that an object should be handled only on the main thread,
 * especially if it's an OpenGL resource.
 */
public interface Destroyable {
	/**
	 * Release any offheap or OpenGL resources held by this object.
	 * MUST be called before this object is allowed to garbage collect, but safe to call multiple times.
	 */
	public void destroy();
}
