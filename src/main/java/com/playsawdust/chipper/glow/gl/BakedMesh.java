package com.playsawdust.chipper.glow.gl;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.playsawdust.chipper.glow.gl.shader.Destroyable;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.pass.MeshPass;
import com.playsawdust.chipper.glow.pass.RenderPass;

public class BakedMesh implements Destroyable {
	private Material material;
	private VertexBuffer mesh;
	private @Nullable RenderPass renderPass;
	
	public BakedMesh(Material material, VertexBuffer mesh, @Nullable RenderPass pass) {
		this.material = material;
		this.mesh = mesh;
		this.renderPass = pass;
	}
	
	/**
	 * Gets this Mesh's Material
	 * @return this Mesh's Material
	 */
	public Material getMaterial() {
		return material;
	}
	
	/**
	 * Gets the OpenGL resource that was generated for this Mesh
	 * @return the VertexBuffer created to represent this mesh on the GPU
	 */
	public VertexBuffer getVertexBuffer() {
		return mesh;
	}
	
	/**
	 * Asks the VertexBuffer referenced by this mesh to draw itself. Does not set material parameters or position!
	 * @param program The currently bound ShaderProgram
	 */
	public void paint(ShaderProgram program) {
		mesh.draw(program);
	}
	
	/**
	 * Gets the RenderPass this mesh has been assigned to, if it was possible to assign this Mesh to a pass in advance, or null if the eventual RenderPass is unknown.
	 * @return The RenderPass this Mesh will be drawn by when scheduled.
	 */
	public @Nullable RenderPass getRenderPass() {
		return renderPass;
	}
	
	@Override
	public void destroy() {
		if (mesh!=null) mesh.destroy();
		mesh = null;
	}

	public void setRenderPass(MeshPass meshPass) {
		this.renderPass = meshPass;
		
	}
}