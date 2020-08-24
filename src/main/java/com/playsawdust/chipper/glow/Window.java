package com.playsawdust.chipper.glow;

import java.nio.IntBuffer;

import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryStack;

public class Window {
	private static final long NULL = 0;
	
	
	private long handle;
	
	/**
	 * NOTE: CALL GLFWINIT BEFORE CREATING A WINDOW!
	 */
	public Window(int width, int height, String title) {
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_TRUE);
		
		handle = GLFW.glfwCreateWindow(width, height, title, NULL, NULL);
		if (handle==NULL) throw new RuntimeException("Window creation failed.");
	}
	
	public long handle() {
		return this.handle;
	}
	
	public void show() {
		GLFW.glfwShowWindow(handle);
	}
	
	public void hide() {
		GLFW.glfwHideWindow(handle);
	}
	
	public void destroy() {
		GLFW.glfwDestroyWindow(handle);
	}
	
	/**
	 * Asks GLFW to make this window's context current
	 */
	public void makeContextCurrent() {
		GLFW.glfwMakeContextCurrent(handle);
	}
	
	/**
	 * This is almost never what you want! This is here so you can init GLFW, then create a Window and RenderScheduler, and then activate a simplified render
	 * loop without ever listing GLFW as a direct dependency. However, you probably want to manually init GLFW, set up error streams and event loops how
	 * YOU want them, etc.
	 */
	public static boolean initGLFW() {
		GLFWErrorCallback.createPrint(System.err).set();
		
		if (GLFW.glfwInit()) {
			return true;
		} else {
			GLFW.glfwTerminate();
			return false;
		}
	}
	
	/*
	public static void startMainLoop(Window window, RenderScheduler scheduler) throws WindowException {
		GLFW.glfwSetKeyCallback(window.handle, (win, key, scancode, action, mods) -> {
			if ( key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE )
				GLFW.glfwSetWindowShouldClose(window.handle, true);
		});
		
		GLFW.glfwMakeContextCurrent(window.handle);
		
		GL.createCapabilities();
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		while ( !GLFW.glfwWindowShouldClose(window.handle) ) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); 
			
			//TODO: Ask scheduler to render us up some frame
			if (scheduler!=null) scheduler.render();
			
			GLFW.glfwSwapBuffers(window.handle);
			
			GLFW.glfwPollEvents();
		}
	}*/
	
	public static class WindowException extends Exception {
		private static final long serialVersionUID = 1939033067467843576L;
		
		public WindowException(String message) {
			super(message);
		}
	}

	public Vector2d getSize(Vector2d sz) {
		try (MemoryStack stack = MemoryStack.stackGet().push()) {
			IntBuffer widthBuffer = stack.ints(1);
			IntBuffer heightBuffer = stack.ints(1);
			GLFW.glfwGetFramebufferSize(handle, widthBuffer, heightBuffer);
			sz.set(widthBuffer.get(), heightBuffer.get());
		}
		return sz;
	}
	
}
