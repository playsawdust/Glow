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
import java.util.Map;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.ImmutableSet;
import com.playsawdust.chipper.glow.event.RunnableEvent;

public class DigitalButtonControl {
	private String name = "unknown";
	private HashMap<Integer, Boolean> keys = new HashMap<>();
	private HashMap<Integer, Boolean> codes = new HashMap<>();
	private HashMap<Integer, Boolean> mouseButtons = new HashMap<>();
	private RunnableEvent onPress = new RunnableEvent();
	private RunnableEvent onRelease = new RunnableEvent();
	private boolean pressed = false;
	private boolean locked = false;
	
	public DigitalButtonControl(String name) {
		this.name = name;
	}
	
	public void handle(int key, int code, int action, int mods) {
		if (action!=GLFW.GLFW_PRESS && action!=GLFW.GLFW_RELEASE) return; //We won't properly handle non-press non-release actions (specifically, GLFW_REPEAT) here
		
		boolean press = (action==GLFW.GLFW_PRESS);
		
		boolean check = false;
		
		if (keys.containsKey(key)) {
			keys.put(key, press);
			check = true;
		}
		
		if (codes.containsKey(code)) {
			codes.put(code, press);
			check = true;
		}
		
		if (check) checkPressed();
	}
	
	public void handleMouse(int button, int action, int mods) {
		if (action!=GLFW.GLFW_PRESS && action!=GLFW.GLFW_RELEASE) return;
		boolean check = false;
		if (mouseButtons.containsKey(button)) {
			boolean press = action==GLFW.GLFW_PRESS;
			if (mouseButtons.get(button)!=press) {
				mouseButtons.put(button, press);
				check = true;
			}
		}
		
		if (check) checkPressed();
	}
	
	private void checkPressed() {
		boolean oldPressed = pressed;
		pressed = false;
		
		for(Map.Entry<Integer, Boolean> entry : keys.entrySet()) {
			if (entry.getValue()) {
				pressed = true;
			}
		}
		
		if (!pressed) {
			for(Map.Entry<Integer, Boolean> entry : codes.entrySet()) {
				if (entry.getValue()) {
					pressed = true;
				}
			}
			
			if (!pressed) {
				for(Map.Entry<Integer, Boolean> entry : mouseButtons.entrySet()) {
					if (entry.getValue()) {
						pressed = true;
						return;
					}
				}
				
				if (!pressed) {
					locked = false;
				}
			}
		}
		
		if (oldPressed & !pressed) {
			onRelease.fire();
		} else if (!oldPressed & pressed) {
			onPress.fire();
		}
	}
	
	public boolean isPressed() {
		return pressed;
	}
	
	public boolean isActive() {
		return pressed & !locked;
	}
	
	/** Lock this key out from becoming active again until all keybinds are simultaneously unpressed. */
	public void lock() {
		locked = true;
	}
	
	public Set<Integer> getKeys() {
		return ImmutableSet.copyOf(keys.keySet());
	}
	
	public Set<Integer> getScanCodes() {
		return ImmutableSet.copyOf(codes.keySet());
	}
	
	public DigitalButtonControl addKey(int key) {
		keys.put(key, false);
		return this;
	}
	
	public DigitalButtonControl addCode(int code) {
		codes.put(code, false);
		return this;
	}
	
	public DigitalButtonControl addMouse(int button) {
		mouseButtons.put(button, false);
		return this;
	}
	
	public void clearBindings() {
		keys.clear();
		codes.clear();
	}
	
	public String getName() {
		return name;
	}
	
	public RunnableEvent onPress() { return onPress; }
	public RunnableEvent onRelease() { return onRelease; }
	
	/** For internal use. Forces this control to release any pressed keys, and fires a single onRelease callback if doing so causes any keys to be released. */
	public void deactivate() {
		boolean fireEvent = false;
		for(Integer i : keys.keySet()) {
			if (keys.get(i)) {
				fireEvent = true;
				keys.put(i, Boolean.FALSE);
			}
		}
		
		for(Integer i : codes.keySet()) {
			if (codes.get(i)) {
				fireEvent = true;
				codes.put(i, Boolean.FALSE);
			}
		}
		
		for(Integer i : mouseButtons.keySet()) {
			if (mouseButtons.get(i)) {
				fireEvent = true;
				mouseButtons.put(i, Boolean.FALSE);
			}
		}
		this.locked = false;
		
		if (fireEvent) onRelease.fire();
	}
	
	/** Clears any existing mappings for this Control and bind to the named GLFW key-constant */
	public void rebindToKey(int key) {
		clearBindings();
		addKey(key);
	}
	
	/** Gets a Control that responds by default to the named GLFW key-constant. */
	public static DigitalButtonControl forKey(String name, int key) {
		DigitalButtonControl control = new DigitalButtonControl(name);
		control.keys.put(key, false);
		return control;
	}
	
	/** Gets a Control that responds to a system-dependent scancode. Use {@link org.lwjgl.glfw.GLFW#glfwGetKeyScancode(int)} to find the scanCode for a named key-constant */
	public static DigitalButtonControl forScanCode(String name, int code) {
		DigitalButtonControl control = new DigitalButtonControl(name);
		control.codes.put(code, false);
		return control;
	}
}
