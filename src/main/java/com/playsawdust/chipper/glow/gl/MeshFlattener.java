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
import org.lwjgl.system.MemoryUtil;

import com.google.common.collect.ImmutableList;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Mesh;

public class MeshFlattener {
	private Layout layout = new Layout();
	private ByteBuffer clientBuffer;
	int vertexCount = 0;
	
	public Layout getLayout() { return layout; }
	
	public void setLayout(Layout layout) {
		this.layout = layout;
	}
	
	/**
	 * Creates a new offheap ByteBuffer holding flattened mesh data matching the layout of this flattener. It is the caller's responsibility
	 * to use {@link MemoryUtil#memFree(java.nio.Buffer)} to free the returned buffer.
	 * @param mesh
	 * @return
	 */
	public ByteBuffer writeMesh(Mesh mesh) {
		if (clientBuffer == null) {
			clientBuffer = MemoryUtil.memAlloc(1024);
		}
		List<MaterialAttribute<?>> attributes = layout.getAttributes();
		for(Mesh.Face face : mesh.faces()) {
			
			for(Mesh.Vertex v : face.vertices()) {
			
				for(MaterialAttribute<?> attribute : attributes) {
					if (clientBuffer.remaining()<layout.getStride(attribute)) {
						clientBuffer = MemoryUtil.memRealloc(clientBuffer, (clientBuffer.capacity()*3)/2);
					}
					BufferWriter<?> writer = layout.getWriterFor(attribute);
					Object o = v.getMaterialAttribute(attribute);
					writer.writeUnsafe(clientBuffer, o);
				}
				
				vertexCount++;
			}
		}
		
		clientBuffer.flip();
		ByteBuffer result = clientBuffer;
		clientBuffer = null;
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
	}
	
	
}
