/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4d;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.playsawdust.chipper.glimmer.ModalContextTree;
import com.playsawdust.chipper.glow.control.ControlSet;
import com.playsawdust.chipper.glow.control.MouseButton;
import com.playsawdust.chipper.glow.event.BiConsumerEvent;
import com.playsawdust.chipper.glow.event.ConsumerEvent;
import com.playsawdust.chipper.glow.event.KeyCallbackEvent;
import com.playsawdust.chipper.glow.event.RunnableEvent;
import com.playsawdust.chipper.glow.event.Vector2dEvent;
import com.playsawdust.chipper.glow.gl.Painter;
import com.playsawdust.chipper.glow.scene.BoundingVolume;
import com.playsawdust.chipper.glow.scene.Scene;
import com.playsawdust.chipper.glow.util.AbstractGPUResource;

public class Window extends AbstractGPUResource {
	private static final long NULL = 0;
	
	/** 60 degrees in radians */
	private static final double SIXTY_DEGREES = 60.0 * (Math.PI/180.0);
	
	private Logger glfwLog = LoggerFactory.getLogger("GLFW");
	private long handle = -1;
	
	private KeyCallbackEvent onRawKey = new KeyCallbackEvent();
	
	private Vector2dEvent onMouseMoved = new Vector2dEvent();
	private BiConsumerEvent<MouseButton, Integer> onMousePressed = new BiConsumerEvent<>();
	private BiConsumerEvent<MouseButton, Integer> onMouseReleased = new BiConsumerEvent<>();
	private RunnableEvent onMouseEntered = new RunnableEvent();
	private RunnableEvent onMouseLeft = new RunnableEvent();
	
	private ArrayList<ControlSet> controlSets = new ArrayList<>();
	
	private int framebufferWidth = 0;
	private int framebufferHeight = 0;
	
	private int windowWidth = 0;
	private int windowHeight = 0;
	
	private double mouseX = 0;
	private double mouseY = 0;
	
	private boolean mouseGrabbed = false;
	
	private RenderScheduler scheduler;
	private Scene scene;
	private ModalContextTree contextTree = new ModalContextTree();
	
	/**
	 * Creates a new Window, and initializes OpenGL and GLFW if needed. (You no longer have to do this manually before the Window is created).
	 * 
	 * When this window is freed, *the OpenGL context will be released*.
	 * 
	 * <p>Note on threading: Windows, like any GPUResource, MUST be created and used ONLY on the program's main thread. Additionally, on OSX, this must be
	 * the very first thread spawned by the application, requiring the {@code -XstartOnFirstThread} JVM argument when running the application.
	 */
	protected Window(int width, int height, String title, boolean special) {
		if (!GLFW.glfwInit()) {
			GLFW.glfwTerminate();
			throw new RuntimeException("Unable to initialize GLFW");
		}
		
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_TRUE);
		if (special) {
			GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE);
			GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
		}
		
		
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
		
		try {
			GLFW.glfwMakeContextCurrent(handle);
			GL.getCapabilities();
		} catch (IllegalStateException ex) {
			GL.createCapabilities();
		}
		
		
		GL11.glViewport(0, 0,framebufferWidth, framebufferHeight);
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL20.GL_MULTISAMPLE);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
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
		
		GLFW.glfwSetMouseButtonCallback(handle, this::handleMouse);
		
		GLFW.glfwSetCursorEnterCallback(handle, (hWin, enter) -> {
			if (enter) {
				onMouseEntered.fire();
			} else {
				onMouseLeft.fire();
			}
		});
		
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GLFW.glfwSwapBuffers(handle);
	}
	
	private void handleKey(long window, int key, int scanCode, int action, int mods) {
		for(ControlSet set : controlSets) {
			set.handleKey(key, scanCode, action, mods);
		}
		
		onRawKey.fire(window, key, scanCode, action, mods);
	}
	
	private void handleMouse(long window, int button, int action, int mods) {
		for(ControlSet set : controlSets) {
			set.handleMouse(button, action, mods);
		}
		
		if (action==GLFW.GLFW_PRESS) {
			onMousePressed.fire(MouseButton.fromGLFW(button), mods);
		} else if (action==GLFW.GLFW_RELEASE) {
			onMouseReleased.fire(MouseButton.fromGLFW(button), mods);
		}
	}
	
	public void setVSync(boolean vsync) {
		if (vsync) {
			GLFW.glfwSwapInterval(1);
		} else {
			GLFW.glfwSwapInterval(0);
		}
	}
	
	/**
	 * Returns an event which can be used to register "raw" key callbacks
	 */
	public KeyCallbackEvent onRawKey() {
		return this.onRawKey;
	}
	
	/** Returns an event which is called whenever the mouse is moved within the window. Callers are given the mouse X and Y coordinates */
	public Vector2dEvent onPostMouseMoved() {
		return this.onMouseMoved;
	}
	
	/** Returns an event which is called whenever a mouse button is pressed. The first argument supplied is the mouse button, and the second is a bitfield of modifiers */
	public BiConsumerEvent<MouseButton, Integer> onMousePressed() {
		return this.onMousePressed;
	}
	
	/** Returns an event which is fired whenever a mouse button is released. The first argument supplied is the mouse button, and the secoind is a bitfield of modifiers */
	public BiConsumerEvent<MouseButton, Integer> onMouseReleased() {
		return this.onMouseReleased;
	}
	
	public ConsumerEvent<Painter> onPaint() {
		return scheduler.onPaint();
	}
	
	/** Adds a ControlSet to this Window. Any controls included will update their state automatically as long as this window is polled. */
	public void addControlSet(ControlSet set) {
		controlSets.add(set);
	}
	
	/** Removes a ControlSet from this Window. It's good practice to setEnabled(false) on the ControlSet before calling this method, so that any currently-pressed controls are released. */
	public void removeControlSet(ControlSet set) {
		controlSets.remove(set);
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
	
	public void setPosition(int x, int y) {
		GLFW.glfwSetWindowPos(handle, x, y);
	}
	
	public Vector2i getPosition() { return getPosition(null); }
	
	public Vector2i getPosition(Vector2i dest) {
		int[] x = new int[1];
		int[] y = new int[1];
		GLFW.glfwGetWindowPos(handle, x, y);
		if (dest==null) dest = new Vector2i();
		dest.x = x[0];
		dest.y = y[0];
		return dest;
	}
	
	/** Gets the RenderScheduler used for this Window's built-in scene */
	public RenderScheduler getRenderScheduler() {
		return scheduler;
	}
	
	/** Sets the RenderScheduler that will be used for this Window's built-in scene */
	public void setRenderScheduler(RenderScheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	public Scene getScene() {
		return scene;
	}
	
	public void setScene(Scene scene) {
		this.scene = scene;
	}
	
	public void setClearColor(int color) {
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >>  8) & 0xFF;
		int b =  color        & 0xFF;
		
		GL11.glClearColor(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
	}
	
	/**
	 * Calling this with {@code true} grabs the mouse on behalf of this Window, hiding and recentering it automatically.
	 * @param mouseGrab
	 */
	public void setMouseGrab(boolean mouseGrab) {
		if (mouseGrab ^ this.mouseGrabbed) {
			if (mouseGrab) {
				GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
			} else {
				GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
			}
			
			this.mouseGrabbed = mouseGrab;
		}
	}
	
	public void _free() {
		if (handle!=-1) {
			GLFW.glfwDestroyWindow(handle);
			handle = -1;
		}
		
		GL.destroy();
		
		GLFW.glfwTerminate();
		GLFW.glfwSetErrorCallback(null).free();
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
	public boolean isMouseGrabbed() { return mouseGrabbed; }
	
	
	public void pollEvents() {
		GLFW.glfwPollEvents();
	}
	
	/**
	 * Renders the Scene in this window and makes it visible. This causes many things to happen:
	 * 
	 * <ul>
	 *   <li>Schedules the Scene on the RenderScheduler
	 *   <li>Triggers an onSchedule event
	 *   <li>Flushes the RenderScheduler to render the scene to the framebuffer or a texture
	 *   <li>Runs any post-process shaders and dumps the result into this Window's framebuffer
	 *   <li>Renders elements in the ModalContextTree
	 *   <li>Triggers an onPaint event
	 *   <li>Swaps this window's buffers, making the content visible on-screen.
	 * </ul>
	 */
	public void render() {
		Matrix4d projection = new Matrix4d();
		projection.setPerspective(SIXTY_DEGREES, framebufferWidth/(double)framebufferHeight, 1, 1000); //TODO: Let user configure near and far clip planes
		scene.setProjectionMatrix(projection);
		
		//TODO: Trigger onPreSchedule
		scene.schedule(scheduler);
		//TODO: Trigger onPostSchedule
		
		
		Matrix4d viewMatrix = new Matrix4d(scene.getProjectionMatrix());
		viewMatrix.mul(new Matrix4d(scene.getCamera().getOrientation(null)));
		
		Vector3d cameraLast = scene.getCamera().getLastPosition(null);
		Vector3d cameraCur = scene.getCamera().getPosition(null);
		Vector3d cameraLerped = cameraLast.lerp(cameraCur, scene.getTimestep().poll()); //overwrites cameraLast
		
		viewMatrix.translate(cameraLerped.mul(-1));
		scheduler.render(viewMatrix); //also triggers painter
		//TODO: maybe split Painter off a little more, get more events in there and allow postprocess shaders
		
		swapBuffers();
	}
	
	/** Swap the framebuffers so that any drawing which was just done becomes visible. */
	public void swapBuffers() {
		GLFW.glfwSwapBuffers(handle);
	}
	
	public Screen getPrimaryScreen() {
		long monitorHandle = GLFW.glfwGetPrimaryMonitor();
		
		return new Screen(monitorHandle);
	}
	
	public List<Screen> getScreens() {
		ArrayList<Screen> result = new ArrayList<>();
		
		PointerBuffer monitorsBuffer = GLFW.glfwGetMonitors();
		if (monitorsBuffer!=null) {
			
			
			while(monitorsBuffer.hasRemaining()) {
				long monitorHandle = monitorsBuffer.get();
				Screen screen = new Screen(monitorHandle);
				result.add(screen);
			}
		}
		
		
		return result;
		
	}
	
	public static Window create(int width, int height, String title) {
		Window result = new Window(width, height, title, false);
		result.scheduler = RenderScheduler.createDefaultScheduler();
		result.scheduler.getPainter().setWindow(result);
		result.scene = new Scene();
		//TODO: Set up and wire everything else
		return result;
	}
	
	public static Window createBare(int width, int height, String title) {
		return new Window(width, height, title, false);
	}
	
	public static Window createNonRectangular(int width, int height, String title) {
		return new Window(width, height, title, true);
	}
	
}
