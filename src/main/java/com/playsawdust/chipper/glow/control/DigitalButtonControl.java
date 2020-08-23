package com.playsawdust.chipper.glow.control;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.ImmutableSet;

public class DigitalButtonControl {
	private String name = "unknown";
	private HashMap<Integer, Boolean> keys = new HashMap<>();
	private HashMap<Integer, Boolean> codes = new HashMap<>();
	private boolean pressed = false;
	private boolean locked = false;
	
	public DigitalButtonControl() {}
	public DigitalButtonControl(String name) {
		this.name = name;
	}
	
	public void handle(int key, int code, int action, int mods) {
		if (action!=GLFW.GLFW_PRESS && action!=GLFW.GLFW_RELEASE) return; //We won't properly handle non-press non-release actions here
		
		boolean press = (action==GLFW.GLFW_PRESS);
		
		if (keys.containsKey(key)) {
			keys.put(key, press);
		}
		
		if (codes.containsKey(code)) {
			codes.put(code, press);
		}
		
		//Go through what looks like a complicated process to resolve the combined state of this Control. Because of the low iteration count, it's very fast.
		for(Map.Entry<Integer, Boolean> entry : keys.entrySet()) {
			if (entry.getValue()) {
				pressed = true;
				return;
			}
		}
		
		for(Map.Entry<Integer, Boolean> entry : codes.entrySet()) {
			if (entry.getValue()) {
				pressed = true;
				return;
			}
		}
		
		pressed = false;
		locked = false;
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
	
	public void clearBindings() {
		keys.clear();
		codes.clear();
	}
	
	/** Clears any existing mappings for this Control and bind to the named GLFW key-constant */
	public void rebindToKey(int key) {
		clearBindings();
		addKey(key);
	}
	
	/** Gets a Control that responds by default to the named GLFW key-constant. */
	public static DigitalButtonControl forKey(int key) {
		DigitalButtonControl control = new DigitalButtonControl();
		control.keys.put(key, false);
		return control;
	}
	
	/** Gets a Control that responds to a system-dependent scancode. Use {@link org.lwjgl.glfw.GLFW#glfwGetKeyScancode(int)} to find the scanCode for a named key-constant */
	public static DigitalButtonControl forScanCode(int code) {
		DigitalButtonControl control = new DigitalButtonControl();
		control.codes.put(code, false);
		return control;
	}
}
