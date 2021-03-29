/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.event;

public class VariableTimestep implements Timestep {
	private long lastTick;
	private int minPeriod;
	private int maxPeriod;
	private long accumulator = 0L;
	
	private ConsumerEvent<Integer> onTick = new ConsumerEvent<>();
	
	public VariableTimestep(int minPeriod, int maxPeriod) {
		if (minPeriod<=0) minPeriod = 1;
		if (maxPeriod<=0) maxPeriod = 1;
		
		this.minPeriod = minPeriod;
		this.maxPeriod = maxPeriod;
		this.lastTick = Timestep.now();
	}
	
	@Override
	public ConsumerEvent<Integer> onTick() {
		return onTick;
	}

	@Override
	public double poll() {
		long now = Timestep.now();
		long elapsed = now - lastTick;
		lastTick = now;
		
		accumulator += elapsed;
		if (accumulator>=minPeriod) {
			int actualPeriod = (int)Math.min(accumulator, maxPeriod);
			
			onTick.fire(actualPeriod);
			
			accumulator -= actualPeriod; //Sync us up with how many millis we actually consumed
			accumulator = accumulator % maxPeriod; //Discard any catch-up ticks
		}
		
		double progress = (double) accumulator / (double) minPeriod;
		if (progress>1.0) progress = 1.0;
		
		return progress;
	}
	
	public static VariableTimestep ofTPS(double minTPS, double maxTPS) {
		double minSecondsPerTick = 1.0 / minTPS;
		double minMillisPerTick = minSecondsPerTick * 1_000.0;
		
		double maxSecondsPerTick = 1.0 / maxTPS;
		double maxMillisPerTick = maxSecondsPerTick * 1_000.0;
		
		System.out.println("Min: "+minMillisPerTick+", Max: "+maxMillisPerTick);
		
		return new VariableTimestep((int) minMillisPerTick, (int) maxMillisPerTick);
	}
}
