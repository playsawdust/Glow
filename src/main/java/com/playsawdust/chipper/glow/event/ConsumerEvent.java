package com.playsawdust.chipper.glow.event;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ConsumerEvent<T> extends Event {
	private ArrayList<Consumer<T>> handlers = new ArrayList<>();
	
	public void register(Consumer<T> r) {
		if (r==null) return;
		handlers.add(r);
	}
	
	public void unregister(Consumer<T> r) {
		handlers.remove(r);
	}
	
	public void unregisterAll() {
		handlers.clear();
	}
	
	public void fire(T t) {
		for(Consumer<T> c : handlers) {
			c.accept(t);
		}
	}
}
