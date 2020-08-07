package com.playsawdust.chipper.glow.gl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3dc;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4dc;
import org.joml.Vector4fc;
import org.joml.Vector4ic;
import org.lwjgl.opengl.GL15;

import com.google.common.collect.ImmutableList;
import com.playsawdust.chipper.glow.model.MaterialAttribute;

public class VertexBuffer {
	private int handle = 0;
	private Layout layout;
	private int vertexCount = 0;
	
	public VertexBuffer(ByteBuffer buf, Layout layout, int vertexCount) {
		this.layout = layout;
		this.vertexCount = vertexCount;
		
		handle = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, handle);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	public Layout getLayout() { return layout; }
	
	public void destroy() {
		if (handle==0) return;
		GL15.glDeleteBuffers(handle);
		handle = 0;
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
		
		public static class Entry<T> {
			private MaterialAttribute<T> sourceData;
			private int glDataClass;
			private int glDataCount;
			private String name;
			private BufferWriter<T> writer;
			private int destBytes = 0;
			
			public Entry(MaterialAttribute<T> sourceData, int glDataClass, String name, BufferWriter<T> writer, int destBytes) {
				this.sourceData = sourceData;
				this.glDataClass = glDataClass;
				
				Class<T> dataClass = sourceData.getDataClass();
				
				if (Vector4dc.class.isAssignableFrom(dataClass) || Vector4fc.class.isAssignableFrom(dataClass) || Vector4ic.class.isAssignableFrom(dataClass)) {
					this.glDataCount = 4;
				} else if (Vector3dc.class.isAssignableFrom(dataClass) || Vector3fc.class.isAssignableFrom(dataClass) || Vector3ic.class.isAssignableFrom(dataClass)) {
					this.glDataCount = 3;
				} else if (Vector2dc.class.isAssignableFrom(dataClass) || Vector2fc.class.isAssignableFrom(dataClass) || Vector2ic.class.isAssignableFrom(dataClass)) {
					this.glDataCount = 2;
				} else {
					this.glDataCount = 1;
				}
				
				this.name = name;
				this.writer = writer;
				this.destBytes = destBytes;
			}
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
	}
}
