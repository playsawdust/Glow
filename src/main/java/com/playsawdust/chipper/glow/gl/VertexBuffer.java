package com.playsawdust.chipper.glow.gl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import com.google.common.collect.ImmutableList;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.model.MaterialAttribute;

public class VertexBuffer {
	private int handle = 0;
	private Layout layout;
	private int vertexCount = 0;
	
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
	
	public void destroy() {
		if (handle==0) return;
		GL20.glDeleteBuffers(handle);
		handle = 0;
	}
	
	public void bind(ShaderProgram prog) {
		GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, handle);
		layout.bind(prog);
	}
	
	public void draw(int program) {
		//GL20.glDrawArrays(GL20.GL_, first, count);
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
				GL20.glVertexAttribPointer(binding, entry.glDataCount, entry.glDataClass, entry.normalized, stride, ofs);
				ofs += entry.destBytes;
			}
				/* //Once we know about the attribs we can fill in the pointers to buffers we upload in this Layout
				//TODO: Cache this info so we're not re-querying it every single time we bind this Layout? That presupposes that Layout won't be shared between programs though... which it shouldn't be. But then we need to say so in the contract.
				int ofs = 0;
				for(Entry<?> entry : entries) {
					GL20.glGetActiveAttrib(program, 0);
					GL20.glVertexAttribPointer(index, entry.destBytes, entry.glDataClass, false, stride, pointer);
				}*/
			
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
			
			public Entry(MaterialAttribute<T> sourceData, int glDataClass, int glDataCount, boolean normalized, String name, BufferWriter<T> writer, int destBytes) {
				this.sourceData = sourceData;
				this.glDataClass = glDataClass;
				this.glDataCount = glDataCount;
				this.normalized = normalized;
				
				//Class<T> dataClass = sourceData.getDataClass();
				/*
				if (Vector4dc.class.isAssignableFrom(dataClass) || Vector4fc.class.isAssignableFrom(dataClass) || Vector4ic.class.isAssignableFrom(dataClass)) {
					this.glDataCount = 4;
				} else if (Vector3dc.class.isAssignableFrom(dataClass) || Vector3fc.class.isAssignableFrom(dataClass) || Vector3ic.class.isAssignableFrom(dataClass)) {
					this.glDataCount = 3;
				} else if (Vector2dc.class.isAssignableFrom(dataClass) || Vector2fc.class.isAssignableFrom(dataClass) || Vector2ic.class.isAssignableFrom(dataClass)) {
					this.glDataCount = 2;
				} else {
					this.glDataCount = 1;
				}*/
				
				this.name = name;
				this.writer = writer;
				this.destBytes = destBytes;
			}
		}
		
	}
}
