package com.playsawdust.chipper.glow.gl;

import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Vertex;

public class MeshFlattener {
	private VertexBuffer.Layout layout = new VertexBuffer.Layout();
	//private ByteBuffer clientBuffer;
	//int vertexCount = 0;
	
	public VertexBuffer.Layout getLayout() { return layout; }
	
	public void setLayout(VertexBuffer.Layout layout) {
		this.layout = layout;
	}
	
	/**
	 * Creates a new offheap ByteBuffer holding flattened mesh data matching the layout of this flattener. It is the caller's responsibility
	 * to use {@link MemoryUtil#memFree(java.nio.Buffer)} to free the returned buffer.
	 * @param mesh
	 * @return
	 */
	public static VertexBuffer uploadMesh(Mesh mesh, VertexBuffer.Layout layout) {
		ByteBuffer clientBuffer = MemoryUtil.memAlloc(bufferSize(mesh, layout));
		int vertexCount = 0;
		
		List<MaterialAttribute<?>> attributes = layout.getAttributes();
		for(Mesh.Face face : mesh.faces()) {
			
			for(Vertex v : face.vertices()) {
			
				for(MaterialAttribute<?> attribute : attributes) {
					if (clientBuffer.remaining()<layout.getStride(attribute)) {
						clientBuffer = MemoryUtil.memRealloc(clientBuffer, (clientBuffer.capacity()*3)/2);
					}
					BufferWriter<?> writer = layout.getWriterFor(attribute);
					Object o = v.getMaterialAttribute(attribute);
					if (o==null) {
						o = mesh.getMaterial().getMaterialAttribute(attribute);
						if (o==null) {
							o = attribute.getDefaultValue();
						}
					}
					writer.writeUnsafe(clientBuffer, o);
				}
				
				vertexCount++;
			}
		}
		
		clientBuffer.flip();
		VertexBuffer result = new VertexBuffer(clientBuffer, layout, vertexCount);
		MemoryUtil.memFree(clientBuffer);
		return result;
	}
	
	public static BakedMesh bake(Mesh mesh, VertexBuffer.Layout layout) {
		VertexBuffer buf = uploadMesh(mesh, layout);
		BakedMesh result = new BakedMesh(mesh.getMaterial(), buf, null);
		return result;
	}
	
	//public VertexBuffer uploadMesh(Mesh mesh) {
	//	MeshFlattener.uploadMesh(mesh, layout);
	//}
	
	/**
	 * Gets the exact size in bytes that the provided Mesh will take up if flattened.
	 * 
	 * <p>NOTE: Flattening will fail at AT MOST 715,827,882 triangles divided by the Layout's byteCount
	 */
	public static int bufferSize(Mesh mesh, VertexBuffer.Layout layout) {
		return layout.getByteCount()*mesh.getFaceCount()*3;
	}
}
