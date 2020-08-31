/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow;

import java.util.function.Consumer;

public class ProgressReport {
	private static final long NANOS_PER_MILLI = 1_000_000L;
	private static final long MILLIS_PER_SECOND = 1_000L;
	
	private long lastReport;
	private Consumer<Integer> consumer;
	
	public ProgressReport(Consumer<Integer> consumer) {
		this.consumer = consumer;
		this.lastReport = now();
		consumer.accept(0);
	}
	
	public void report(int cur, int max) {
		long now = now();
		//System.out.println("Time since last report: "+(now-lastReport)+" now: "+now);
		if (now-lastReport >= MILLIS_PER_SECOND) {
			consumer.accept(percent((double)cur/max));
			lastReport = now;
		}
	}
	
	public static long now() {
		return System.nanoTime() / NANOS_PER_MILLI;
	}
	
	public static int percent(Double f) {
		if (f>1.0) return 100;
		if (f<0.0) return 0;
		return (int)(f * 100);
	}
}
