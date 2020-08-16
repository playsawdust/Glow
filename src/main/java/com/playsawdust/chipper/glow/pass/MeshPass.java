package com.playsawdust.chipper.glow.pass;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.playsawdust.chipper.glow.gl.BakedMesh;
import com.playsawdust.chipper.glow.gl.MeshFlattener;
import com.playsawdust.chipper.glow.gl.VertexBuffer;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;

public class MeshPass implements RenderPass {
	
	private ArrayList<MeshEntry> scheduled = new ArrayList<>();
	private ArrayList<BakedMesh> scheduledForDeletion = new ArrayList<>();
	private Cache<Mesh, BakedMesh> bakedMeshes = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.SECONDS)
			.<Mesh, BakedMesh>removalListener((notification)-> {
				if (notification.getCause()==RemovalCause.EXPIRED) {
					scheduledForDeletion.add(notification.getValue());
				}
			})
			.build();
	private ShaderProgram shader;
	private VertexBuffer.Layout layout = new VertexBuffer.Layout();
	private String id;
	
	public MeshPass(String id) {
		this.id = id;
	}
	
	public void setLayout(VertexBuffer.Layout layout) {
		this.layout = layout;
	}
	
	public void setShader(ShaderProgram shader) {
		this.shader = shader;
	}
	
	@Override
	public void apply() {
		if (shader==null) return;
		shader.bind();
		Matrix4d viewMatrix = new Matrix4d();
		viewMatrix.identity();
		shader.setUniform("viewMatrix", viewMatrix);
		
		Matrix4d modelMatrix = new Matrix4d();
		Matrix4d rotTransfer = new Matrix4d();
		for(MeshEntry entry : scheduled) {
			rotTransfer.identity();
			rotTransfer.set(entry.orientation);
			modelMatrix.identity();
			modelMatrix.translate(entry.position);
			modelMatrix.mul(rotTransfer);
			shader.setUniform("modelMatrix", modelMatrix);
			
			entry.baked.paint(shader);
		}
		
		scheduled.clear();
		
		//Clear out VBOs hit with cache eviction
		for(BakedMesh mesh : scheduledForDeletion) {
			mesh.destroy();
		}
		scheduledForDeletion.clear();
	}

	@Override
	public boolean canEnqueue(Object o) {
		return (o instanceof Model) || (o instanceof Mesh) || (o instanceof BakedMesh); //&& matchesOtherOptions, e.g. shader, opacity
	}

	@Override
	public void enqueue(Object o, Vector3dc position, Matrix3dc orientation) {
		if (o instanceof BakedMesh) {
			scheduled.add(new MeshEntry((BakedMesh)o, position, orientation));
		} else if (o instanceof Model) {
			for(Mesh mesh : ((Model)o).meshes()) {
				enqueue(mesh, position, orientation);
			}
		} else if (o instanceof Mesh) {
			BakedMesh baked;
			try {
				baked = bakedMeshes.get((Mesh)o, ()->{
					BakedMesh result = MeshFlattener.bake((Mesh)o, layout);
					result.setRenderPass(this);
					return result;
				});
				
				scheduled.add(new MeshEntry(baked, position, orientation));
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	
	private static class MeshEntry {
		BakedMesh baked;
		Vector3d position = new Vector3d(0,0,0);
		Matrix3d orientation = new Matrix3d();
		
		public MeshEntry(BakedMesh baked, Vector3dc position, Matrix3dc orientation) {
			this.baked = baked;
			this.position.set(position);
			this.orientation.set(orientation);
		}
	}


	@Override
	public BakedMesh bake(Mesh mesh) {
		BakedMesh result = MeshFlattener.bake(mesh, layout);
		result.setRenderPass(this);
		return result;
	}

	@Override
	public String getId() {
		return this.id;
	}

	
}
