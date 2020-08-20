package com.playsawdust.chipper.glow.voxel;

@FunctionalInterface
public interface VecFunction<T> {
	public T apply(int x, int y, int z);
}
