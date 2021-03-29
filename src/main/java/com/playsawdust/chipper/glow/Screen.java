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

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import com.playsawdust.chipper.glow.util.RectangleI;

/**
 * This object isn't a GPUResource, but creating it invokes GPU methods, so this object MUST only be created on the main thread.
 * Generally it is useful to obtain instances of this class from {@link Window#getPrimaryScreen()} and / or {@link Window#getScreens()}
 */
public class Screen {
	private static final double INCHES_PER_MM = 0.0393701;
	
	private String name;
	private int widthMM;
	private int heightMM;
	private int widthPixels;
	private int heightPixels;
	private double contentScaleX;
	private double contentScaleY;
	private double dpiX;
	private double dpiY;
	private int refreshRate;
	private RectangleI workArea;
	
	/*package*/ Screen(long handle) {
		name = GLFW.glfwGetMonitorName(handle);
		
		try ( MemoryStack stack = MemoryStack.stackPush() ) {
			IntBuffer width = stack.mallocInt(1);
			IntBuffer height = stack.mallocInt(1);
			
			GLFW.glfwGetMonitorPhysicalSize(handle, width, height);
			widthMM = width.get();
			heightMM = height.get();
			
			FloatBuffer floatX = stack.mallocFloat(1);
			FloatBuffer floatY = stack.mallocFloat(1);
			
			GLFW.glfwGetMonitorContentScale(handle, floatX, floatY);
			
			contentScaleX = floatX.get();
			contentScaleY = floatY.get();
			
			IntBuffer x = stack.mallocInt(1);
			IntBuffer y = stack.mallocInt(1);
			width.clear();
			height.clear();
			
			GLFW.glfwGetMonitorWorkarea(handle, x, y, width, height);
			this.workArea = new RectangleI(x.get(), y.get(), width.get(), height.get());
			
			GLFWVidMode videoMode = GLFW.glfwGetVideoMode(handle); // Allocated and freed by GLFW
			widthPixels = videoMode.width();
			heightPixels = videoMode.height();
			
			double widthInches = widthMM*INCHES_PER_MM;
			dpiX = widthPixels / widthInches;
			
			double heightInches = heightMM*INCHES_PER_MM;
			dpiY = heightPixels / heightInches;
			
			refreshRate = videoMode.refreshRate();
		}
	}
	
	
	public String getName() { return this.name; }
	
	/** Gets this Screen's physical width, in millimeters. */
	public int getPhysicalWidth() { return this.widthMM; }
	/** Gets this Screen's physical height, in millimeters. */
	public int getPhysicalHeight() { return this.heightMM; }
	
	public boolean isSquarePixels() {
		return contentScaleX==contentScaleY;
	}
	
	public double getDPI() {
		return (int)Math.max(dpiX, dpiY);
	}
	
	public double getContentScale() {
		return (int)Math.max(contentScaleX, contentScaleY);
	}
	
	public double getDPIX() {
		return dpiX;
	}
	
	public double getDPIY() {
		return dpiY;
	}
	
	public RectangleI getWorkArea() {
		return new RectangleI(workArea);
	}
	
	public int getWidth() {
		return widthPixels;
	}
	
	public int getHeight() {
		return heightPixels;
	}
	
	public int getRefreshRate() {
		return refreshRate;
	}
}
