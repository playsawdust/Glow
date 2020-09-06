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
