package com.playsawdust.chipper.glow.util;

import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;

public class Histogram<K> {
	private HashMap<K, Integer> map = new HashMap<>();
	
	public Histogram() {
		
	}
	
	public void add(K k) {
		Integer i = map.get(k);
		if (i==null) i = 0;
		map.put(k, i+1);
	}
	
	public int get(K k) {
		Integer i = map.get(k);
		return (i==null) ? 0 : i;
	}
	
	/** Get the most frequent element in the set, or the highest peak in the histogram, or null if the histogram is empty. */
	public @Nullable K getMode() {
		int bestCount = 0;
		K bestK = null;
		
		for(Map.Entry<K, Integer> entry : map.entrySet()) {
			if (entry.getValue()>bestCount) {
				bestCount = entry.getValue();
				bestK = entry.getKey();
			}
		}
		
		return bestK;
	}
	
	/** Gets the number of *distinct* elements in this histogram, not counting repeats */
	public int size() {
		return map.size();
	}
}
