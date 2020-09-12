/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.gl;


/**
 * Classes that implement this interface are associated with one or more native/offheap resources. These resources must be explicitly freed when a class instance is
 * no longer used, by calling the {@link #free} method.
 *
 * <p>This interface extends {@link AutoCloseable}, which means that implementations may be used as resources in try-with-resources statements.</p>
 */
public interface OffheapResource extends AutoCloseable {
	/**
	 * Release any offheap resources held by this object.
	 * MUST be called before this object is allowed to garbage collect, but is safe to call multiple times.
	 */
	public void free();
	
	@Override
	public default void close() throws Exception {
		free();
	}
}
