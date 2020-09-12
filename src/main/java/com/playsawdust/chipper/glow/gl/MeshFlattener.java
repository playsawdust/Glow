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
import java.util.Iterator;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import com.playsawdust.chipper.glow.model.Face;
import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Vertex;

public class MeshFlattener {
	private VertexBuffer.Layout layout = new VertexBuffer.Layout();
	
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
		VertexBufferData buf = new VertexBufferData();
		buf.layout = layout;
		buf.ensureCapacity(bufferSize(mesh, layout));
		
		buf.beginWriting();
		{
			writeMesh(buf, mesh, layout);
		}
		buf.endWriting();
		
		VertexBuffer result = new VertexBuffer(buf.buffer(), layout, buf.numVertices);
		buf.free();
		return result;
	}
	
	/**
	 * Writes a flattened mesh into a clientside vertex buffer
	 * @param buf The buffer to write mesh data into
	 * @param mesh The mesh to write
	 * @param layout The layout of vertex attributes
	 */
	public static void writeMesh(VertexBufferData buf, Mesh mesh, VertexBuffer.Layout layout) {
		int vertexCount = 0;
		int bytesPerVertex = layout.getByteCount();
		
		List<MaterialAttribute<?>> attributes = layout.getAttributes();
		Material material = mesh.getMaterial();
		for(Face face : mesh.faces()) {
			if (face.vertexCount()==3) {
				buf.ensureCapacity(bytesPerVertex*3);
				for(Vertex v : face) {
					writeVertex(v, material, layout, attributes, buf.buffer());
					vertexCount++;
				}
			} else if (face.vertexCount()==4) {
				buf.ensureCapacity(bytesPerVertex*6);
				
				//There's an easy tesselation for this
				Iterator<Vertex> iterator = face.iterator();
				Vertex a = iterator.next();
				Vertex b = iterator.next();
				Vertex c = iterator.next();
				Vertex d = iterator.next();
				
				writeVertex(a, material, layout, attributes, buf.buffer());
				writeVertex(b, material, layout, attributes, buf.buffer());
				writeVertex(c, material, layout, attributes, buf.buffer());
				
				writeVertex(a, material, layout, attributes, buf.buffer());
				writeVertex(c, material, layout, attributes, buf.buffer());
				writeVertex(d, material, layout, attributes, buf.buffer());
				
				vertexCount += 6;
			} else {
				buf.ensureCapacity(bytesPerVertex*(face.vertexCount()-2)*3);
				//Triangle fan, triangle fan, does whatever triangles can
				Iterator<Vertex> iterator = face.iterator();
				Vertex a = iterator.next();
				Vertex prev = iterator.next();
				while(iterator.hasNext()) {
					Vertex cur = iterator.next();
					writeVertex(a, material, layout, attributes, buf.buffer());
					writeVertex(prev, material, layout, attributes, buf.buffer());
					writeVertex(cur, material, layout, attributes, buf.buffer());
					
					prev = cur;
					vertexCount+=3;
				}
			}
		}
		buf.numVertices += vertexCount;
	}
	
	public static void writeVertex(Vertex v, Material material, VertexBuffer.Layout layout, List<MaterialAttribute<?>> attributes, ByteBuffer buffer) {
		for(MaterialAttribute<?> attribute : attributes) {
			if (buffer.remaining()<layout.getStride(attribute)) {
				buffer = MemoryUtil.memRealloc(buffer, (buffer.capacity()*3)/2);
			}
			BufferWriter<?> writer = layout.getWriterFor(attribute);
			Object o = v.getMaterialAttribute(attribute);
			if (o==null) {
				o = material.getMaterialAttribute(attribute);
				if (o==null) {
					o = attribute.getDefaultValue();
				}
			}
			writer.writeUnsafe(buffer, o);
		}
	}
	
	/**
	 * Uploads a Mesh and packages it with material data so that a RenderScheduler or a MeshPass can render it straight from the GPU.
	 * @param mesh The Mesh to upload
	 * @param layout The layout of vertex attributes needed for the shader to understand the uploaded flattened Mesh
	 * @return A BakedMesh which can then be scheduled to render the Mesh straight from the GPU without uploading it again.
	 */
	public static BakedMesh bake(Mesh mesh, VertexBuffer.Layout layout) {
		VertexBuffer buf = uploadMesh(mesh, layout);
		BakedMesh result = new BakedMesh(mesh.getMaterial(), buf, null);
		return result;
	}
	
	/**
	 * Gets the exact size in bytes that the provided Mesh will take up if flattened, assuming that every mesh face is a triangle.
	 * 
	 * <p>NOTE: Flattening will fail at AT MOST 715,827,882 triangles divided by the Layout's byteCount
	 */
	public static int bufferSize(Mesh mesh, VertexBuffer.Layout layout) {
		return layout.getByteCount()*mesh.getFaceCount()*4;
	}
}
