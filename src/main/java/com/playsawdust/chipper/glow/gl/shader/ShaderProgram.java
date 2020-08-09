package com.playsawdust.chipper.glow.gl.shader;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import com.playsawdust.chipper.glow.gl.GLType;

public class ShaderProgram {
	private int vertexHandle = -1;
	private int fragmentHandle = -1;
	private int handle = -1;
	
	/* Note: An attribute's binding is its "location" in the list, which usually corresponds to its binding location */
	private ArrayList<Entry> attributes = new ArrayList<>();
	private HashMap<String, Integer> bindings = new HashMap<>();
	
	public ShaderProgram(String vertex, String fragment) throws ShaderError {
		vertexHandle = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		GL20.glShaderSource(vertexHandle, vertex);
		GL20.glCompileShader(vertexHandle);
		
		//Detect and throw vertex shader errors
		int vertexStatus = GL20.glGetShaderi(vertexHandle, GL20.GL_COMPILE_STATUS);
		if (vertexStatus == GL20.GL_FALSE) {
			String result = GL20.glGetShaderInfoLog(vertexHandle);
			GL20.glDeleteShader(vertexHandle); //cleanup before exiting
			throw new ShaderError("An error occurred compiling the vertex shader.", result);
		} else {
			//TODO: WARN somehow for any non-error shader logs?
		}
		
		fragmentHandle = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		GL20.glShaderSource(fragmentHandle, fragment);
		GL20.glCompileShader(fragmentHandle);
		
		//Detect and throw fragment shader errors
		int fragmentStatus = GL20.glGetShaderi(fragmentHandle, GL20.GL_COMPILE_STATUS);
		if (fragmentStatus == GL20.GL_FALSE) {
			String result = GL20.glGetShaderInfoLog(fragmentHandle);
			GL20.glDeleteShader(vertexHandle); //cleanup *both* before exiting
			GL20.glDeleteShader(fragmentHandle);
			throw new ShaderError("An error occurred compiling the fragment shader.", result);
		} else {
			//TODO: WARN somehow for any non-error shader logs?
		}
		
		handle = GL20.glCreateProgram();
		GL20.glAttachShader(handle, vertexHandle);
		GL20.glAttachShader(handle, fragmentHandle);
		GL20.glLinkProgram(handle);
		int linkStatus = GL20.glGetProgrami(handle, GL20.GL_LINK_STATUS);
		if (linkStatus == GL20.GL_FALSE) {
			String result = GL20.glGetProgramInfoLog(handle);
			GL20.glDeleteShader(vertexHandle); //cleanup *all* before exiting
			GL20.glDeleteShader(fragmentHandle);
			GL20.glDeleteProgram(handle);
			throw new ShaderError("An error occurred linking the shader program.", result);
		} else {
			//TODO: WARN somehow for any non-error shader logs?
		}
		
		//TODO: Query vertex attribs and uniforms!
		int attributeCount = GL20.glGetProgrami(handle, GL20.GL_ACTIVE_ATTRIBUTES);
		//System.out.println("Attribute count: "+attributeCount);
		try (MemoryStack stackFrame = MemoryStack.stackPush()) {
			IntBuffer sizeBuf = stackFrame.mallocInt(1);
			IntBuffer typeBuf = stackFrame.mallocInt(1);
			for(int i=0; i<attributeCount; i++) {
				String name = GL20.glGetActiveAttrib(handle, i, sizeBuf, typeBuf);
				int size = sizeBuf.get(0);
				int type = typeBuf.get(0);
				int location = GL20.glGetAttribLocation(handle, name);
				
				//System.out.println("    Name: "+name+", Type: "+GLType.of(type)+", I: "+i+", Binding: "+location);
				
				Entry entry = new Entry(name, location, type);
				attributes.add(entry);
				bindings.put(name, location);
			}
		}
	}
	
	/**
	 * Gets the <em>program</em> handle. The individual shaders will have different handles.
	 */
	public int handle() {
		return this.handle;
	}
	
	public int getVertexHandle() {
		return vertexHandle;
	}
	
	public int getFragmentHandle() {
		return fragmentHandle;
	}
	
	/** Binds this ShaderProgram to be used for subsequent draw calls. Silently fails if the program has been deleted. */
	public void bind() {
		if (handle!=-1) GL20.glUseProgram(handle);
	}
	
	public void destroy() {
		if (handle==-1) return;
		
		//Should be covered by the DeleteProgram but just crossing the T's and dotting the I's
		GL20.glDetachShader(handle, vertexHandle);
		GL20.glDetachShader(handle, fragmentHandle);
		
		GL20.glDeleteProgram(handle);
		GL20.glDeleteShader(vertexHandle);
		GL20.glDeleteShader(fragmentHandle);
		
		handle = -1;
		vertexHandle = -1;
		fragmentHandle = -1;
	}
	
	/**
	 * Returns the binding location for the named vertex attribute. If no binding exists, -1 is returned.
	 * @param name the name of the generic vertex attribute to find a binding for
	 * @return the binding location for the attribute, or -1 if no binding exists.
	 */
	public int getAttribBinding(String name) {
		Integer loc =  bindings.get(name);
		if (loc==null) return -1;
		return loc;
	}
	
	public @Nullable GLType getAttribType(String name) {
		for(Entry entry : attributes) {
			if (entry.name.equals(name)) {
				return GLType.of(entry.type);
			}
		}
		return null;
	}
	
	private static class Entry {
		public String name;
		public int binding; //TODO: Redundant?
		public int type;
		
		public Entry(String name, int binding, int type) {
			this.name = name;
			this.type = type;
		}
	}
}
