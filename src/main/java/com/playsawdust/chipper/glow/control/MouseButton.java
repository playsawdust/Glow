/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.control;

import org.lwjgl.glfw.GLFW;

public enum MouseButton {
	LEFT(GLFW.GLFW_MOUSE_BUTTON_LEFT),
	RIGHT(GLFW.GLFW_MOUSE_BUTTON_RIGHT),
	MIDDLE(GLFW.GLFW_MOUSE_BUTTON_MIDDLE),
	BUTTON4(GLFW.GLFW_MOUSE_BUTTON_4),
	BUTTON5(GLFW.GLFW_MOUSE_BUTTON_5),
	BUTTON6(GLFW.GLFW_MOUSE_BUTTON_6),
	BUTTON7(GLFW.GLFW_MOUSE_BUTTON_7),
	BUTTON8(GLFW.GLFW_MOUSE_BUTTON_8),
	UNKNOWN(-1);
	
	private int id;
	
	MouseButton(int id) {
		this.id = id;
	}
	
	public int toGLFW() {
		return id;
	}
	
	public static MouseButton fromGLFW(int id) {
		for(MouseButton button : values()) {
			if (id==button.id) return button;
		}
		return UNKNOWN;
	}
}
