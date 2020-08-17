package com.playsawdust.chipper.glow;

import java.util.ArrayList;

import org.joml.Matrix3dc;
import org.joml.Vector3dc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.playsawdust.chipper.glow.gl.BakedMesh;
import com.playsawdust.chipper.glow.gl.BakedModel;
import com.playsawdust.chipper.glow.gl.BufferWriter;
import com.playsawdust.chipper.glow.gl.VertexBuffer;
import com.playsawdust.chipper.glow.model.ImmutableModel;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.MaterialAttributeContainer;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;
import com.playsawdust.chipper.glow.pass.MeshPass;
import com.playsawdust.chipper.glow.pass.RenderPass;

public class RenderScheduler {
	private ArrayList<RenderPass> passes = new ArrayList<>();
	
	/**
	 * Schedule some renderable object
	 * @param o
	 * @return true if the object was scheduled for render.
	 */
	public boolean schedule(Object o, Vector3dc position, Matrix3dc orientation, MaterialAttributeContainer environment) {
		if (o instanceof ImmutableModel) {
			return schedule((ImmutableModel)o, position, orientation, environment);
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
		for(Mesh mesh : m.meshes()) {
			for(RenderPass pass : passes) {
				if (pass.canEnqueue(mesh)) {
					meshes.add(pass.bake(mesh));
					break;
				}
			}
		}
		return new BakedModel(meshes);
	}
	
	public void render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		for(RenderPass pass : passes) {
			pass.apply();
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
		solidPass.setLayout(layout);
		
		solidPass.layoutUniform(MaterialAttribute.AMBIENT_LIGHT, "ambientLight");
		
		result.passes.add(solidPass);
		
		return result;
	}

	public RenderPass getPass(String string) {
		for(RenderPass pass : passes) {
			if (pass.getId().equals(string)) return pass;
		}
		return null;
	}
}
