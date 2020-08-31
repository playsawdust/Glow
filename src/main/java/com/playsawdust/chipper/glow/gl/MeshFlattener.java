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
		ByteBuffer buf = MemoryUtil.memAlloc(bufferSize(mesh, layout));
		int vertexCount = 0;
		int bytesPerVertex = layout.getByteCount();
		
		List<MaterialAttribute<?>> attributes = layout.getAttributes();
		Material material = mesh.getMaterial();
		for(Face face : mesh.faces()) {
			if (face.vertexCount()==3) {
				buf = ensureCapacity(buf, bytesPerVertex*3);
				for(Vertex v : face) {
					writeVertex(v, material, layout, attributes, buf);
					vertexCount++;
				}
			} else if (face.vertexCount()==4) {
				buf = ensureCapacity(buf, bytesPerVertex*6);
				
				//There's an easy tesselation for this
				Iterator<Vertex> iterator = face.iterator();
				Vertex a = iterator.next();
				Vertex b = iterator.next();
				Vertex c = iterator.next();
				Vertex d = iterator.next();
				
				writeVertex(a, material, layout, attributes, buf);
				writeVertex(b, material, layout, attributes, buf);
				writeVertex(c, material, layout, attributes, buf);
				
				writeVertex(a, material, layout, attributes, buf);
				writeVertex(c, material, layout, attributes, buf);
				writeVertex(d, material, layout, attributes, buf);
				
				vertexCount += 6;
			} else {
				buf = ensureCapacity(buf, bytesPerVertex*(face.vertexCount()-2)*3);
				//Triangle fan, triangle fan, does whatever triangles can
				Iterator<Vertex> iterator = face.iterator();
				Vertex a = iterator.next();
				Vertex prev = iterator.next();
				while(iterator.hasNext()) {
					Vertex cur = iterator.next();
					writeVertex(a, material, layout, attributes, buf);
					writeVertex(prev, material, layout, attributes, buf);
					writeVertex(cur, material, layout, attributes, buf);
					
					prev = cur;
					vertexCount+=3;
				}
			}
		}
		
		buf.flip();
		VertexBuffer result = new VertexBuffer(buf, layout, vertexCount);
		MemoryUtil.memFree(buf);
		return result;
	}
	
	private static ByteBuffer ensureCapacity(ByteBuffer buf, int extra) {
		int remaining = buf.capacity()-buf.position();
		if (extra>remaining) {
			return MemoryUtil.memRealloc(buf, buf.capacity()*3/2);
		} else {
			return buf;
		}
	}
	
	private static void writeVertex(Vertex v, Material material, VertexBuffer.Layout layout, List<MaterialAttribute<?>> attributes, ByteBuffer buffer) {
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
