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

public class RunnableEvent extends Event {
	private ArrayList<Runnable> handlers = new ArrayList<>();
	
	public void register(Runnable r) {
		if (r==null) return;
		handlers.add(r);
	}
	
	public void unregister(Runnable r) {
		handlers.remove(r);
	}
	
	public void unregisterAll() {
		handlers.clear();
	}
	
	public void fire() {
		for(Runnable r : handlers) {
			r.run();
		}
	}
}
