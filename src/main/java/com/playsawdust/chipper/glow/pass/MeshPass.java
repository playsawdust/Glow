package com.playsawdust.chipper.glow.pass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.playsawdust.chipper.glow.RenderScheduler;
import com.playsawdust.chipper.glow.gl.BakedMesh;
import com.playsawdust.chipper.glow.gl.MeshFlattener;
import com.playsawdust.chipper.glow.gl.Texture;
import com.playsawdust.chipper.glow.gl.VertexBuffer;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.MaterialAttributeContainer;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;

public class MeshPass implements RenderPass {
	
	private ArrayList<MeshEntry> scheduled = new ArrayList<>();
	private ArrayList<BakedMesh> scheduledForDeletion = new ArrayList<>();
	private Cache<Mesh, BakedMesh> bakedMeshes = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.SECONDS)
			.<Mesh, BakedMesh>removalListener((notification)-> {
				//We no longer check the cause because the cache is ONLY ever used for automatically-generated BakedMeshes
				//if (notification.getCause()==RemovalCause.EXPIRED || notification.getCause()==RemovalCause.EXPLICIT) {
					scheduledForDeletion.add(notification.getValue());
				//}
			})
			.build();
	private ShaderProgram shader;
	private VertexBuffer.Layout layout = new VertexBuffer.Layout();
	private HashMap<MaterialAttribute<?>, String> uniformLayout = new HashMap<>();
	private String id;
	
	public MeshPass(String id) {
		this.id = id;
	}
	
	public void setLayout(VertexBuffer.Layout layout) {
		this.layout = layout;
	}
	
	public void layoutUniform(MaterialAttribute<?> attribute, String uniform) {
		uniformLayout.put(attribute, uniform);
	}
	
	public void setShader(ShaderProgram shader) {
		this.shader = shader;
	}
	
	
	
	@Override
	public void apply(Matrix4dc viewMatrix, RenderScheduler scheduler) {
		if (shader==null) return;
		shader.bind();
		shader.setUniform("viewMatrix", viewMatrix);
		
		Matrix4d modelMatrix = new Matrix4d();
		Matrix4d rotTransfer = new Matrix4d();
		MaterialAttributeContainer lastEnvironment = null;
		Material lastMaterial = null;
		for(MeshEntry entry : scheduled) {
			if (lastEnvironment==null || !entry.environment.equals(lastEnvironment)) {
				applyContainer(entry.environment, scheduler);
				lastEnvironment = entry.environment;
				lastMaterial = null; //We'll need to reapply the material even if it's the same to clobber duplicate attributes
			}
			if (lastMaterial==null || lastMaterial!=(entry.baked.getMaterial())) {
				//System.out.println("Applying material: ");
				//System.out.println(entry.baked.getMaterial().toString());
				applyContainer(entry.baked.getMaterial(), scheduler);
				lastMaterial = entry.baked.getMaterial();
			}
			
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
	public void enqueue(Object o, Vector3dc position, Matrix3dc orientation, MaterialAttributeContainer environment) {
		if (o instanceof BakedMesh) {
			scheduled.add(new MeshEntry((BakedMesh)o, position, orientation, environment));
		} else if (o instanceof Model) {
			for(Mesh mesh : ((Model)o).meshes()) {
				enqueue(mesh, position, orientation, environment);
			}
		} else if (o instanceof Mesh) {
			BakedMesh baked;
			try {
				baked = bakedMeshes.get((Mesh)o, ()->{
					BakedMesh result = MeshFlattener.bake((Mesh)o, layout);
					result.setRenderPass(this);
					return result;
				});
				
				scheduled.add(new MeshEntry(baked, position, orientation, environment));
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	
	private static class MeshEntry {
		BakedMesh baked;
		Vector3d position = new Vector3d(0,0,0);
		Matrix3d orientation = new Matrix3d();
		MaterialAttributeContainer environment;
		
		public MeshEntry(BakedMesh baked, Vector3dc position, Matrix3dc orientation, MaterialAttributeContainer environment) {
			this.baked = baked;
			this.position.set(position);
			this.orientation.set(orientation);
			this.environment = environment;
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

	private void applyContainer(MaterialAttributeContainer container, RenderScheduler renderScheduler) {
		for(MaterialAttribute<?> attrib : uniformLayout.keySet()) {
			//System.out.println(attrib);
			Object obj = container.getMaterialAttribute(attrib);
			if (obj==null) continue;
			if (obj instanceof Vector3dc) {
				shader.setUniform(uniformLayout.get(attrib), (Vector3dc)obj);
			} else if (obj instanceof Double) {
				shader.setUniform(uniformLayout.get(attrib), (Double)obj);
			}
		}
		
		String tex = container.getMaterialAttribute(MaterialAttribute.DIFFUSE_TEXTURE_ID);
		if (tex!=null) {
			Texture diffuse = renderScheduler.getTexture(tex);
			if (diffuse!=null) {
				diffuse.bind(shader, "materialDiffuseTexture", 0); //TODO: Consider not hardcoding 0
			}
		}
	}

	@Override
	public void destroy() {
		bakedMeshes.invalidateAll();
		bakedMeshes.cleanUp();
		for(BakedMesh mesh : scheduledForDeletion) {
			mesh.destroy();
		}
		scheduledForDeletion.clear();
	}
}
