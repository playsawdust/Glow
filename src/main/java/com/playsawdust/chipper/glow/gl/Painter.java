package com.playsawdust.chipper.glow.gl;

import java.util.ArrayList;

import org.joml.Matrix4d;
import org.joml.Vector2d;
import org.lwjgl.opengl.GL20;

import com.playsawdust.chipper.glow.Window;
import com.playsawdust.chipper.glow.gl.VertexBuffer.Layout;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.model.Material;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.Vertex;
import com.playsawdust.chipper.glow.util.AbstractCombinedResource;
import com.playsawdust.chipper.glow.util.RectangleI;

public class Painter extends AbstractCombinedResource {
	protected Matrix4d ortho = new Matrix4d();//.setOrtho2DLH(0, window.width, window.height, 0);
	
	protected VertexBuffer buffer;
	protected VertexBufferData accumulator;
	protected ShaderProgram program;
	protected Window window;
	
	public Painter(Layout layout, ShaderProgram program) {
		buffer = VertexBuffer.createStreaming(layout);
		accumulator = new VertexBufferData();
		accumulator.layout = layout;
		this.program = program;
		
	}
	
	public Layout getLayout() {
		return buffer.getLayout();
	}
	
	public void beginPainting() {
		if (program!=null) program.bind();
		ortho.setOrtho2D(0, window.getWidth(), window.getHeight(), 0);
		GL20.glEnable(GL20.GL_BLEND);
		GL20.glBlendFuncSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		//ortho.identity();
		//ortho.translate(-1, 1, 0);
		//ortho.scale(1.0/windowSize.x, -1.0/windowSize.y, 1);
		
	}
	
	public void endPainting() {
		
	}
	
	public void paintTexture(Texture tex, RectangleI tile, int x, int y, int color) {
		paintTexture(tex, x, y, tile.width(), tile.height(), tile.x(), tile.y(), tile.width(), tile.height(), color);
	}
	
	public void paintTexture(Texture tex, int x, int y) {
		paintTexture(tex, x, y, tex.getWidth(), tex.getHeight(), 0, 0, tex.getWidth(), tex.getHeight(), 0xFF_FFFFFF);
	}
	
	/** Paints the texture on the screen immediately. All coordinates are in pixels or texels. */
	public void paintTexture(Texture tex, int x, int y, int width, int height, int texX, int texY, int texWidth, int texHeight, int color) {
		accumulator.beginWriting();
		
		double texelX = 1.0 / (double) tex.getWidth();
		double texelY = 1.0 / (double) tex.getHeight();
		
		
		writeVertex(x, y+height, texX * texelX, texY * texelY, color);
		writeVertex(x+width, y+height, (texX+texWidth) * texelX, texY * texelY, color);
		writeVertex(x+width, y, (texX+texWidth) * texelX, (texY+texHeight) * texelY, color);
		
		writeVertex(x, y+height, texX * texelX, texY * texelY, color);
		writeVertex(x+width, y, (texX+texWidth) * texelX, (texY+texHeight) * texelY, color);
		writeVertex(x, y, texX * texelX, (texY+texHeight) * texelY, color);
		
		accumulator.endWriting();
		
		buffer.uploadStreaming(accumulator.buffer(), accumulator.vertexCount());
		
		program.setUniform("viewMatrix", ortho);
		tex.bind(program, "tex", 0);
		
		buffer.draw(program);
	}
	
	public void paintString(Font font, int x, int y, CharSequence str, int color) {
		
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
	public void _free() {
		if (buffer!=null) {
			buffer.free();
			buffer = null;
		}
		
		if (accumulator!=null) {
			accumulator.free();
			accumulator = null;
		}
		
		if (program!=null) {
			program.free();
			program = null;
		}
	}

	public void setShaderProgram(ShaderProgram prog) {
		this.program = prog;
	}
	
	public void setWindow(Window window) {
		this.window = window;
	}
}
