package com.playsawdust.chipper.glow.event;

public interface Timestep {
	/**
	 * Gets the onTick Event which fires when this Timestep determines that a physics tick should occur. Event consumers will receive the number of elapsed milliseconds since the last tick. For fixed Timesteps this will always be the period.
	 * @return the onTick Event which fires when this Timestep determines that a physics tick should occur.
	 */
	public ConsumerEvent<Integer> onTick();
	
	/**
	 * Polls this Timestep, firing the onTick Event on this thread if it determines taht a physics tick should occur.
	 * @return how far we are through the current tick, from 0 at the previous tick to 1 at the current tick.
	 */
	public double poll();
	
	public static long now() {
		return System.nanoTime() / 1_000_000L;
	}
}
