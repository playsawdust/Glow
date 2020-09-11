package com.playsawdust.chipper.glow.gl;

import java.util.ArrayList;

import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Vector2d;
import org.joml.Vector3d;

import com.playsawdust.chipper.glow.Window;
import com.playsawdust.chipper.glow.gl.VertexBuffer.Layout;
import com.playsawdust.chipper.glow.gl.shader.Destroyable;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.model.Face;
import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Vertex;

public class Painter implements Destroyable {
	protected Matrix4d ortho = new Matrix4d();//.setOrtho2DLH(0, window.width, window.height, 0);
	
	protected VertexBuffer buffer;
	protected ClientVertexBuffer accumulator;
	protected ShaderProgram program;
	protected Window window;
	
	public Painter(Layout layout, ShaderProgram program) {
		buffer = VertexBuffer.createStreaming(layout);
		accumulator = new ClientVertexBuffer();
		accumulator.layout = layout;
		this.program = program;
		
	}
	
	public Layout getLayout() {
		return buffer.getLayout();
	}
	
	public void beginPainting() {
		if (program!=null) program.bind();
		Vector2d windowSize = window.getSize(new Vector2d());
		ortho.setOrtho2D(0, windowSize.x, windowSize.y, 0);
		//ortho.identity();
		//ortho.translate(-1, 1, 0);
		//ortho.scale(1.0/windowSize.x, -1.0/windowSize.y, 1);
		
	}
	
	public void endPainting() {
		
	}
	
	/** Paints the texture on the screen immediately. All coordinates are in pixels or texels. */
	public void paintTexture(Texture tex, int x, int y, int width, int height, int texX, int texY, int texWidth, int texHeight, int color) {
		accumulator.beginWriting();
		
		double texelX = 1.0 / (double) tex.getWidth();
		double texelY = 1.0 / (double) tex.getHeight();
		
		
		writeVertex(x, y+height, texX * texelX, (texY+texHeight) * texelY, color);
		writeVertex(x+width, y+height, (texX+texWidth) * texelX, (texY+texHeight) * texelY, color);
		writeVertex(x+width, y, (texX+texWidth) * texelX, texY * texelY, color);
		
		writeVertex(x, y+height, texX * texelX, (texY+texHeight) * texelY, color);
		writeVertex(x+width, y, (texX+texWidth) * texelX, texY * texelY, color);
		writeVertex(x, y, texX * texelX, texY * texelY, color);
		
		/*
		Vertex a = new Vertex(new Vector2d(x, y), new Vector2d(texX * texelX, texY * texelY));
		Vertex b = new Vertex(new Vector2d(x+width, y), new Vector2d((texX+texWidth) * texelX, texY * texelY));
		Vertex c = new Vertex(new Vector2d(x+width, y+height), new Vector2d((texX+texWidth) * texelX, (texY+texHeight) * texelY));
		Vertex d = new Vertex(new Vector2d(x, y+height), new Vector2d(texX * texelX, (texY+texHeight) * texelY));
		
		Mesh mesh = new Mesh();
		mesh.setMaterial(Material.BLANK);
		mesh.addFace(new Face(a, d, c, b));
		
		MeshFlattener.writeMesh(accumulator, mesh, accumulator.layout);*/
		accumulator.endWriting();
		
		buffer.uploadStreaming(accumulator.buffer(), accumulator.vertexCount());
		
		program.setUniform("viewMatrix", ortho);
		tex.bind(program, "tex", 0);
		
		buffer.draw(program);
	}
	
	
	@SuppressWarnings("unused")
	private void writeVertexSlow(double x, double y, double u, double v, int argb) {
		Vertex vert = new Vertex(new Vector2d(x,y), new Vector2d(u,v));
		vert.putMaterialAttribute(MaterialAttribute.ARGB_COLOR, argb);
		MeshFlattener.writeVertex(vert, Material.BLANK, buffer.getLayout(), new ArrayList<>(), accumulator.buffer());
	}
	
	private void writeVertex(double x, double y, double u, double v, int argb) {
		accumulator.ensureCapacity(20);
		accumulator.buffer().putFloat((float)x);
		accumulator.buffer().putFloat((float)y);
		accumulator.buffer().putFloat((float)u);
		accumulator.buffer().putFloat((float)v);
		accumulator.buffer().put((byte)((argb >> 24) & 0xFF));
		accumulator.buffer().put((byte)((argb >> 16) & 0xFF));
		accumulator.buffer().put((byte)((argb >>  8) & 0xFF));
		accumulator.buffer().put((byte)((argb      ) & 0xFF));
		accumulator.numVertices++;
	}
	
	@Override
	public void destroy() {
		if (buffer!=null) {
			buffer.destroy();
			buffer = null;
		}
	}

	public void setShaderProgram(ShaderProgram prog) {
		this.program = prog;
	}
	
	public void setWindow(Window window) {
		this.window = window;
	}
}
