/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.event;

public class FixedTimestep implements Timestep {
	private long lastTick;
	private long period;
	private long accumulator = 0L;
	
	private ConsumerEvent<Integer> onTick = new ConsumerEvent<>();
	
	public FixedTimestep(long period) {
		if (period<=0) throw new IllegalArgumentException("Period cannot be <= 0");
		this.lastTick = Timestep.now();
		this.period = period;
	}
	
	@Override
	public double poll() {
		long now = Timestep.now();
		long elapsed = now - lastTick;
		lastTick = now;
		
		accumulator += elapsed;
		if (accumulator>=period) {
			onTick.fire((int)period);
			accumulator = accumulator % period; //Discard any catch-up ticks
		}
		
		return (double) accumulator / (double) period;
	}
	
	@Override
	public ConsumerEvent<Integer> onTick() {
		return onTick;
	}
	
	public static FixedTimestep ofTPS(int tps) {
		double secondsPerTick = 1.0 / (double) tps;
		double millisPerTick = secondsPerTick * 1_000.0;
		if (millisPerTick<0) millisPerTick = 1;
		return new FixedTimestep((long)millisPerTick);
	}
}
