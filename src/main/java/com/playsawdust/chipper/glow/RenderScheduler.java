/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow;

import java.util.ArrayList;
import java.util.HashMap;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Matrix3dc;
import org.joml.Matrix4dc;
import org.joml.Vector2dc;
import org.joml.Vector3dc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.playsawdust.chipper.glow.gl.BakedMesh;
import com.playsawdust.chipper.glow.gl.BakedModel;
import com.playsawdust.chipper.glow.gl.BufferWriter;
import com.playsawdust.chipper.glow.gl.Texture;
import com.playsawdust.chipper.glow.gl.VertexBuffer;
import com.playsawdust.chipper.glow.gl.shader.Destroyable;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.MaterialAttributeContainer;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;
import com.playsawdust.chipper.glow.pass.MeshPass;
import com.playsawdust.chipper.glow.pass.RenderPass;

public class RenderScheduler implements Destroyable {
	private ArrayList<RenderPass> passes = new ArrayList<>();
	private HashMap<String, Texture> textures = new HashMap<>();
	
	/**
	 * Schedule some renderable object
	 * @param o
	 * @return true if the object was scheduled for render.
	 */
	public boolean schedule(Object o, Vector3dc position, Matrix3dc orientation, MaterialAttributeContainer environment) {
		if (o instanceof BakedModel) {
			return schedule((BakedModel)o, position, orientation, environment);
		}
		
		for(RenderPass pass : passes) {
			if (pass.canEnqueue(o)) {
				pass.enqueue(o, position, orientation, environment);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean schedule(BakedModel model, Vector3dc position, Matrix3dc orientation, MaterialAttributeContainer environment) {
		boolean allScheduled = true;
		for(BakedMesh mesh : model) {
			RenderPass preferred = mesh.getRenderPass();
			if (preferred!=null) {
				preferred.enqueue(mesh, position, orientation, environment);
			} else {
				boolean scheduled = false;
				for(RenderPass pass : passes) {
					if (pass.canEnqueue(mesh)) {
						pass.enqueue(mesh, position, orientation, environment);
						scheduled = true;
						break;
					}
				}
				allScheduled &= scheduled;
			}
		}
		
		return allScheduled;
	}
	
	public boolean schedule(Model m, Vector3dc position, Matrix3dc orientation, MaterialAttributeContainer environment) {
		for(RenderPass pass : passes) {
			if (pass.canEnqueue(m)) {
				pass.enqueue(m, position, orientation, environment);
				return true;
			}
		}
		
		return false;
	}
	
	public BakedModel bake(Model m) {
		ArrayList<BakedMesh> meshes = new ArrayList<>();
		for(Mesh mesh : m) {
			if (mesh.isEmpty()) continue;
			for(RenderPass pass : passes) {
				if (pass.canEnqueue(mesh)) {
					meshes.add(pass.bake(mesh));
					break;
				}
			}
		}
		return new BakedModel(meshes);
	}
	
	public void render(Matrix4dc viewMatrix) {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		for(RenderPass pass : passes) {
			pass.apply(viewMatrix, this);
		}
	}
	
	public static RenderScheduler createDefaultScheduler() {
		RenderScheduler result = new RenderScheduler();
		
		MeshPass solidPass = new MeshPass("solid");
		VertexBuffer.Layout layout = new VertexBuffer.Layout();
		
		VertexBuffer.Layout.Entry<Vector3dc> positionEntry =
				VertexBuffer.Layout.Entry
				.forAttribute(MaterialAttribute.POSITION)
				.named("position")
				.withLayout(GL20.GL_FLOAT, 3)
				.nonNormalized()
				.withWriter(BufferWriter.WRITE_VEC3_TO_FLOATS);
		
		layout.addVertexAttribute(positionEntry);
		
		VertexBuffer.Layout.Entry<Vector3dc> normalEntry =
				VertexBuffer.Layout.Entry
				.forAttribute(MaterialAttribute.NORMAL)
				.named("normal")
				.withLayout(GL20.GL_FLOAT, 3)
				.nonNormalized()
				.withWriter(BufferWriter.WRITE_VEC3_TO_FLOATS);
		layout.addVertexAttribute(normalEntry);
		
		VertexBuffer.Layout.Entry<Vector2dc> uvEntry =
				VertexBuffer.Layout.Entry
				.forAttribute(MaterialAttribute.UV)
				.named("uv")
				.withLayout(GL20.GL_FLOAT, 2)
				.nonNormalized()
				.withWriter(BufferWriter.WRITE_VEC2_TO_FLOATS);
		layout.addVertexAttribute(uvEntry);
		
		solidPass.setLayout(layout);
		
		solidPass.layoutUniform(MaterialAttribute.AMBIENT_LIGHT, "ambientLight");
		solidPass.layoutUniform(MaterialAttribute.DIFFUSE_COLOR, "materialColor");
		solidPass.layoutUniform(MaterialAttribute.SPECULARITY, "materialSpecularity");
		solidPass.layoutUniform(MaterialAttribute.EMISSIVITY, "materialEmissivity");
		
		result.passes.add(solidPass);
		
		return result;
	}

	public RenderPass getPass(String string) {
		for(RenderPass pass : passes) {
			if (pass.getId().equals(string)) return pass;
		}
		return null;
	}

	@Override
	public void destroy() {
		for(Texture t : textures.values()) {
			t.destroy();
		}
		textures.clear();
		
		for(RenderPass pass : passes) {
			pass.destroy();
		}
	}
	
	public void registerTexture(String id, Texture texture) {
		textures.put(id, texture);
	}
	
	public @Nullable Texture getTexture(String id) {
		return textures.get(id);
	}
}
