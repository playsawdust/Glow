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

/** Special case of BiConsumerEvent which accepts primitive double x and y coordinates */
public class Vector2dEvent extends Event {
private ArrayList<Handler> handlers = new ArrayList<>();
	
	public void register(Handler r) {
		if (r==null) return;
		handlers.add(r);
	}
	
	public void unregister(Handler r) {
		handlers.remove(r);
	}
	
	public void unregisterAll() {
		handlers.clear();
	}
	
	public void fire(double x, double y) {
		for(Handler c : handlers) {
			c.accept(x, y);
		}
	}
	
	public static interface Handler {
		public void accept(double x, double y);
	}
}
