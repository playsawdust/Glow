/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.control;

import java.util.HashMap;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.lwjgl.glfw.GLFW;

public class ControlSet {
	protected HashMap<String, DigitalButtonControl> controls = new HashMap<>();
	
	public void mapWASD() {
		map("up", GLFW.GLFW_KEY_W);
		map("down", GLFW.GLFW_KEY_S);
		map("left", GLFW.GLFW_KEY_A);
		map("right", GLFW.GLFW_KEY_D);
	}
	
	/** Adds a DigitalButtonControl with name {@code name} and maps it by default to the specified GLFW key constant */
	public void map(String name, int keyCode) {
		DigitalButtonControl control = new DigitalButtonControl(name).addKey(keyCode);
		controls.put(name, control);
	}
	
	/** Adds a DigitalButtonControl with name {@code name} and maps it by default to the specified GLFW mouse button constant */
	public void mapMouse(String name, int button) {
		DigitalButtonControl control = new DigitalButtonControl(name).addMouse(button);
		controls.put(name, control);
	}
	
	public @Nullable DigitalButtonControl getButton(String name) {
		return controls.get(name);
	}
	
	public void addButton(DigitalButtonControl control) {
		controls.put(control.getName(), control);
	}
	
	/** Feed me GLFW keyCallback input! */
	public void handleKey(int key, int scanCode, int action, int mods) {
		for(DigitalButtonControl control : controls.values()) control.handle(key, scanCode, action, mods);
	}
	
	public void handleMouse(int button, int action, int mods) {
		for(DigitalButtonControl control : controls.values()) control.handleMouse(button, action, mods);
	}
	
	public boolean isPressed(String button) {
		DigitalButtonControl control = controls.get(button);
		return (control==null) ? false : control.isPressed();
	}
	
	public boolean isActive(String button) {
		DigitalButtonControl control = controls.get(button);
		return (control==null) ? false : control.isActive();
	}
	
	public void lock(String button) {
		DigitalButtonControl control = controls.get(button);
		if (control!=null) control.lock();
	}
}
