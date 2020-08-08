package com.playsawdust.chipper.glow.gl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

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
	
	public int handle() {
		return this.handle;
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
		public void bind(int program) {
			int curProgram = GL20.glGetInteger(GL20.GL_CURRENT_PROGRAM);
			System.out.println("Current Program: "+curProgram);
			program = curProgram; //TODO HAHA SUCKERS
			
			int attributes = GL20.glGetProgrami(program, GL20.GL_ACTIVE_ATTRIBUTES);
			System.out.println("The program has "+attributes+" vertex attributes.");
			try (MemoryStack stackFrame = MemoryStack.stackPush()) {
				IntBuffer sizeBuf = stackFrame.mallocInt(1);
				IntBuffer typeBuf = stackFrame.mallocInt(1);
				for(int i=0; i<attributes; i++) {
					String name = GL20.glGetActiveAttrib(program, i, sizeBuf, typeBuf);
					int size = sizeBuf.get(0);
					int type = typeBuf.get(0);
					
					System.out.println("Name: "+name+", Size: "+size+", Type: "+type);
				}
				/* //Once we know about the attribs we can fill in the pointers to buffers we upload in this Layout
				//TODO: Cache this info so we're not re-querying it every single time we bind this Layout? That presupposes that Layout won't be shared between programs though... which it shouldn't be. But then we need to say so in the contract.
				int ofs = 0;
				for(Entry<?> entry : entries) {
					GL20.glGetActiveAttrib(program, 0);
					GL20.glVertexAttribPointer(index, entry.destBytes, entry.glDataClass, false, stride, pointer);
				}*/
			}
		}
		
		
		
		
		
		
		public static class Entry<T> {
			private MaterialAttribute<T> sourceData;
			private int glDataClass;
			private boolean normalized;
			private String name;
			private BufferWriter<T> writer;
			private int destBytes = 0;
			
			public Entry(MaterialAttribute<T> sourceData, int glDataClass, boolean normalized, String name, BufferWriter<T> writer, int destBytes) {
				this.sourceData = sourceData;
				this.glDataClass = glDataClass;
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
