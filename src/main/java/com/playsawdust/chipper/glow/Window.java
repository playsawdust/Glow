/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow;

import java.nio.IntBuffer;

import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.playsawdust.chipper.glow.event.BiConsumerEvent;
import com.playsawdust.chipper.glow.event.KeyCallbackEvent;
import com.playsawdust.chipper.glow.util.AbstractGPUResource;

public class Window extends AbstractGPUResource {
	private static final long NULL = 0;
	
	private Logger glfwLog = LoggerFactory.getLogger("GLFW");
	private long handle = -1;
	
	private KeyCallbackEvent onKey = new KeyCallbackEvent();
	private BiConsumerEvent<Double, Double> onMouseMoved = new BiConsumerEvent<>();
	
	private int framebufferWidth = 0;
	private int framebufferHeight = 0;
	
	private int windowWidth = 0;
	private int windowHeight = 0;
	
	private double mouseX = 0;
	private double mouseY = 0;
	
	/**
	 * NOTE: CALL GLFWINIT BEFORE CREATING A WINDOW!
	 */
	public Window(int width, int height, String title) {
		if (!GLFW.glfwInit()) {
			GLFW.glfwTerminate();
			throw new RuntimeException("Unable to initialize GLFW");
		}
		
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_TRUE);
		
		this.framebufferWidth = width;
		this.framebufferHeight = height;
		
		handle = GLFW.glfwCreateWindow(width, height, title, NULL, NULL);
		if (handle==NULL) throw new RuntimeException("Window creation failed.");
		
		//Grab out the sizes of the content pane and the window
		try (MemoryStack stack = MemoryStack.stackGet().push()) {
			IntBuffer widthBuffer = stack.ints(1);
			IntBuffer heightBuffer = stack.ints(1);
			GLFW.glfwGetFramebufferSize(handle, widthBuffer, heightBuffer);
			this.framebufferWidth = widthBuffer.get();
			this.framebufferHeight = heightBuffer.get();
			
			widthBuffer.clear();
			heightBuffer.clear();
			GLFW.glfwGetWindowSize(handle, widthBuffer, heightBuffer);
			this.windowWidth = widthBuffer.get();
			this.windowHeight = heightBuffer.get();
		}
		
		//TODO: Find a better way to test if capabilities have been created.
		try {
			GLFW.glfwMakeContextCurrent(handle);
			GL.getCapabilities();
		} catch (IllegalStateException ex) {
			GL.createCapabilities();
		}
		
		
		GL11.glViewport(0, 0,framebufferWidth, framebufferHeight);
		
		GLFW.glfwSetErrorCallback( (err, desc)->{
				String errorString = GLFWErrorCallback.getDescription(desc);
				glfwLog.error(errorString, new RuntimeException()); //Exception created/included here in order to generate a stack trace which can be inspected and abbreviated by a Logger
			});
		
		GLFW.glfwSetKeyCallback(handle, this::handleKey);
		
		GLFW.glfwSetFramebufferSizeCallback(handle, (hWin, qwidth, qheight) -> {
			this.framebufferWidth = qwidth;
			this.framebufferHeight = qheight;
			
			GL11.glViewport(0, 0, qwidth, qheight);
		});
		
		GLFW.glfwSetWindowSizeCallback(handle, (hWin, qwidth, qheight) -> {
			this.windowWidth = qwidth;
			this.windowHeight = qheight;
		});
		
		GLFW.glfwSetCursorPosCallback(handle, (hWin, x, y) -> {
			this.mouseX = x;
			this.mouseY = y;
			this.onMouseMoved.fire(x, y);
		});
		
		//TODO: Setup all other callbacks
	}
	
	private void handleKey(long window, int key, int scanCode, int action, int mods) {
		//TODO: Invoke controls
		
		onKey.fire(window, key, scanCode, action, mods);
	}
	
	/**
	 * Returns an event which can be used to register "raw" key callbacks
	 */
	public KeyCallbackEvent onRawKey() {
		return this.onKey;
	}
	
	public BiConsumerEvent<Double, Double> onMouseMoved() {
		return this.onMouseMoved;
	}
	
	public long handle() {
		checkFreed();
		return this.handle;
	}
	
	public void show() {
		checkFreed();
		GLFW.glfwShowWindow(handle);
	}
	
	public void hide() {
		checkFreed();
		GLFW.glfwHideWindow(handle);
	}
	
	public void _free() {
		if (handle!=-1) {
			GLFW.glfwDestroyWindow(handle);
			handle = -1;
		}
	}
	
	/** Returns the width of the <em>framebuffer</em> managed by this Window. The Window itself may be larger! */
	public int getWidth() { return framebufferWidth; }
	/** Returns the height of the <em>framebuffer</em> managed by this Window. The Window itself may be larger! */
	public int getHeight() { return framebufferHeight; }
	/** Returns the width of this <em>Window</em>. You can get the size of the drawable region using {@link #getWidth()} */
	public int getWindowWidth() { return windowWidth; }
	/** Returns the height of this <em>Window</em>. You can get the size of the drawable region using {@link #getHeight()} */
	public int getWindowHeight() { return windowHeight; }
	
	public double getMouseX() { return mouseX; }
	public double getMouseY() { return mouseY; }
	
	
	
	
	/**
	 * Asks GLFW to make this window's context current
	 */
	/*
	public void makeContextCurrent() {
		checkFreed();
		GLFW.glfwMakeContextCurrent(handle);
	}*/
	
}
