/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.gl;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.playsawdust.chipper.glow.gl.VertexBuffer.Layout;
import com.playsawdust.chipper.glow.util.AbstractOffheapResource;

public class VertexBufferData extends AbstractOffheapResource {
	Layout layout;
	private ByteBuffer buf;
	int numVertices = 0;
	
	@Override
	public void _free() {
		if (buf!=null) MemoryUtil.memFree(buf);
		buf = null;
		numVertices = 0;
	}
	
	public void ensureCapacity(int extra) {
		if (buf==null) {
			buf = MemoryUtil.memAlloc(Math.max(extra, 1024));
		}
		
		int remaining = buf.capacity()-buf.position();
		if (extra>remaining) {
			int toRealloc = Math.max(remaining+extra, buf.capacity()*3/2);
			buf = MemoryUtil.memRealloc(buf, toRealloc);
		}
	}
	
	/**
	 * Finishes writing to the buffer
	 */
	public void endWriting() {
		if (buf==null) return;
		buf.flip();
	}
	
	/**
	 * Clears the written-vertex count, resets the underlying buffer's position to zero, clears its mark,
	 * and sets its limit to its capacity, so that new data can be written to the buffer. Its capacity
	 * remains the same.
	 */
	public void beginWriting() {
		if (buf==null) buf = MemoryUtil.memAlloc(1024);
		buf.clear();
		numVertices = 0;
	}
	
	public ByteBuffer buffer() {
		return buf;
	}

	public int vertexCount() {
		return numVertices;
	}
}