/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.playsawdust.chipper.glow.UseAfterFreeException;
import com.playsawdust.chipper.glow.gl.GPUResource;

public abstract class AbstractGPUResource implements GPUResource {
	private static final Logger log = LoggerFactory.getLogger("NativeResources");
	protected boolean freed = false;
	
	protected abstract void _free();
	
	@Override
	public final void free() {
		if (freed) return;
		try {
			_free();
		} finally {
			freed = true;
		}
	}
	
	/**
	 * @return {@code true} if {@link #free} has been called and this resource
	 * 		is no longer valid
	 */
	public boolean isFreed() {
		return freed;
	}
	
	/**
	 * @throws UseAfterFreeException if this resource has been {@link #free freed}
	 */
	protected void checkFreed() throws UseAfterFreeException {
		if (freed) throw new UseAfterFreeException();
	}
	
	@SuppressWarnings("deprecation")
	protected void finalize() throws Throwable {
		super.finalize();
		if (!freed) {
			log.warn("An offheap resource, {}, is being garbage collected, but it was never explicitly freed!\n"
					+ "Before releasing references to a native resource, you should call free to ensure it is immediately deallocated.", getClass().getSimpleName());
			free();
		}
	}
}
