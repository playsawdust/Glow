package com.playsawdust.chipper.glow.event;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFWKeyCallbackI;

public class KeyCallbackEvent extends Event {
	private ArrayList<GLFWKeyCallbackI> handlers = new ArrayList<>();
	
	public void register(GLFWKeyCallbackI r) {
		if (r==null) return;
		handlers.add(r);
	}
	
	public void unregister(GLFWKeyCallbackI r) {
		handlers.remove(r);
	}
	
	public void unregisterAll() {
		handlers.clear();
	}
	
	public void fire(long window, int key, int scanCode, int action, int mods) {
		for(GLFWKeyCallbackI handler : handlers) {
			handler.invoke(window, key, scanCode, action, mods);
		}
	}
}
