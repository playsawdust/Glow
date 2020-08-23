package com.playsawdust.chipper.glow.control;

import java.util.HashMap;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.lwjgl.glfw.GLFW;

public class ControlSet {
	private HashMap<String, DigitalButtonControl> controls = new HashMap<>();
	
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
	
	public @Nullable DigitalButtonControl getButton(String name) {
		return controls.get(name);
	}
	
	/** Feed me GLFW keyCallback input! */
	public void handleKey(int key, int scanCode, int action, int mods) {
		for(DigitalButtonControl control : controls.values()) control.handle(key, scanCode, action, mods);
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
