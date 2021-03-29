/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
