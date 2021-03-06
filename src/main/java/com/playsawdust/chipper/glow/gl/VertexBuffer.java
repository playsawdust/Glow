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
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.google.common.collect.ImmutableList;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.util.AbstractGPUResource;

public class VertexBuffer extends AbstractGPUResource {
	private int handle = 0;
	private Layout layout;
	private int vertexCount = 0;
	private int primitive = GL11.GL_TRIANGLES;
	
	private VertexBuffer() {}
	
	public VertexBuffer(ByteBuffer buf, Layout layout, int vertexCount) {
		this.layout = layout;
		this.vertexCount = vertexCount;
		
		handle = GL20.glGenBuffers();
		GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, handle);
		GL20.glBufferData(GL20.GL_ARRAY_BUFFER, buf, GL20.GL_STATIC_DRAW);
		GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
	}
	
	public int handle() {
		return this.handle;
	}
	
	public Layout getLayout() { return layout; }
	
	@Override
	public void _free() {
		if (handle==0) return;
		GL20.glDeleteBuffers(handle);
		handle = 0;
	}
	
	public VertexBuffer uploadStreaming(ByteBuffer buf, int vertexCount) {
		this.vertexCount = vertexCount;
		GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, handle);
		GL20.glBufferData(GL20.GL_ARRAY_BUFFER, buf, GL20.GL_STREAM_DRAW);
		GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		
		return this;
	}
	
	public void draw(ShaderProgram prog) {
		GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, handle);
		layout.bind(prog);
		GL20.glDrawArrays(primitive, 0, vertexCount);
	}
	
	public void draw(ShaderProgram prog, int first, int count) {
		if (first<0 || count<=0) throw new IllegalArgumentException();
		if (first>=vertexCount) return; //*do not* draw ranges of vertices which do not exist
		
		if (first+count>vertexCount) {
			count = vertexCount-first; //*do* draw any covered vertices which physically exist
		}
		GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, handle);
		layout.bind(prog);
		GL20.glDrawArrays(primitive, first, count);
	}
	
	public static VertexBuffer createStreaming(Layout layout) {
		VertexBuffer result = new VertexBuffer();
		
		result.layout = layout;
		result.handle = GL20.glGenBuffers();
		
		return result;
	}
	
	public static class Layout {
		private ArrayList<Entry<?>> entries = new ArrayList<>();
		
		/**
		 * Gets the number of vertex attributes in this Layout
		 * @return the number of vertex attributes in this Layout
		 */
		public int size() {
			return entries.size();
		}
		
		public int getStride(MaterialAttribute<?> attribute) {
			for(Entry<?> entry : entries) {
				if (entry.sourceData.equals(attribute)) return entry.destBytes;
			}
			return 0;
		}

		public List<MaterialAttribute<?>> getAttributes() {
			ImmutableList.Builder<MaterialAttribute<?>> builder = ImmutableList.builder();
			for(Entry<?> entry : entries) {
				builder.add(entry.sourceData);
			}
			return builder.build();
		}
		
		public <T> void addVertexAttribute(Entry<T> entry) {
			entries.add(entry);
		}
		
		public MaterialAttribute<?> getAttribute(int index) {
			return entries.get(index).sourceData;
		}
		
		/**
		 * Returns the appropriate BufferWriter for this VertexAttribute
		 * @param <T>
		 * @param attribute
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public <T> BufferWriter<T> getWriterFor(MaterialAttribute<T> attribute) {
			for(Entry<?> entry : entries) {
				if (entry.sourceData.equals(attribute)) return (BufferWriter<T>)entry.writer;
			}
			return null;
		}
		
		public int getByteCount() {
			int result = 0;
			for(Entry<?> entry : entries) {
				result += entry.destBytes;
			}
			
			return result;
		}
		
		/**
		 * Issues a series of glBufferPointer calls that describe attributes in this Layout to lwjgl.
		 */
		public void bind(ShaderProgram program) {
			int stride = getByteCount();
			int ofs = 0;
			for(Entry<?> entry : entries) {
				int binding = program.getAttribBinding(entry.name);
				GL20.glEnableVertexAttribArray(binding);
				if (binding<0) { //Failed binding. This stretch of data will be ignored.
					continue;
				}
				GL20.glEnableVertexAttribArray(binding);
				GL20.glVertexAttribPointer(binding, entry.glDataCount, entry.glDataClass, entry.normalized, stride, ofs);
				
				ofs += entry.destBytes;
			}
		}
		
		
		
		
		
		
		public static class Entry<T> {
			private MaterialAttribute<T> sourceData;
			/** The data type of each value presented to glVertexAttribPointer. For example, GL_FLOAT for an attribute of GLType.VEC3F */
			private int glDataClass;
			/** The number of values to list for glVertexAttribPointer. For Example, 3 for an attribute of GLType.VEC3F*/
			private int glDataCount;
			private boolean normalized;
			private String name;
			private BufferWriter<T> writer;
			private int destBytes = 0;
			
			private Entry() {}
			
			public Entry(MaterialAttribute<T> sourceData, int glDataClass, int glDataCount, boolean normalized, String name, BufferWriter<T> writer, int destBytes) {
				this.sourceData = sourceData;
				this.glDataClass = glDataClass;
				this.glDataCount = glDataCount;
				this.normalized = normalized;
				
				this.name = name;
				this.writer = writer;
				this.destBytes = destBytes;
			}
			
			public Entry<T> named(String name) {
				this.name = name;
				return this;
			}
			
			public Entry<T> withLayout(int dataType, int dataCount) {
				GLType type = GLType.of(dataType);
				if (type!=GLType.UNKNOWN) {
					int sz = type.getSize();
					if (sz!=-1) this.destBytes = sz * dataCount;
				} else {
					System.out.println("Warning! Unknown layout type! Please call withDestBytes on the Layout Entry");
				}
				this.glDataClass = dataType;
				this.glDataCount = dataCount;
				return this;
			}
			
			public Entry<T> normalized() {
				normalized = true;
				return this;
			}
			
			public Entry<T> nonNormalized() {
				normalized = false;
				return this;
			}
			
			public Entry<T> withWriter(BufferWriter<T> writer) {
				this.writer = writer;
				return this;
			}
			
			public Entry<T> withDestBytes(int size) {
				this.destBytes = size;
				return this;
			}
			
			public static <T> Entry<T> forAttribute(MaterialAttribute<T> attribute) {
				Entry<T> result = new Entry<>();
				result.sourceData = attribute;
				
				return result;
			}
		}
		
	}
}
