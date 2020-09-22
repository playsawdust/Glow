package com.playsawdust.chipper.glow.util;

import com.google.common.base.Preconditions;

public final class MathUtil {
	private MathUtil() {}
	
	public static double min(double i1, double i2, double i3) {
		return Math.min(i1, Math.min(i2, i3));
	}
	
	public static double min(double i1, double i2, double i3, double i4) {
		return Math.min(Math.min(i1, i2), Math.min(i3, i4));
	}
	
	public static double min(double... list) {
		Preconditions.checkArgument(list.length>0);
		
		double result = list[0];
		for(double d : list) {
			result = Math.min(result, d);
		}
		
		return result;
	}
	
	public static int min(int i1, int i2, int i3) {
		return Math.min(i1, Math.min(i2, i3));
	}
	
	public static int min(int i1, int i2, int i3, int i4) {
		return Math.min(Math.min(i1, i2), Math.min(i3, i4));
	}
	
	public static int min(int... list) {
		Preconditions.checkArgument(list.length>0);
		
		int result = list[0];
		for(int d : list) {
			result = Math.min(result, d);
		}
		
		return result;
	}
	
	public static double max(double i1, double i2, double i3) {
		return Math.max(i1, Math.max(i2, i3));
	}
	
	public static double max(double i1, double i2, double i3, double i4) {
		return Math.max(Math.max(i1, i2), Math.max(i3, i4));
	}
	
	public static double max(double... list) {
		Preconditions.checkArgument(list.length>0);
		
		double result = list[0];
		for(double d : list) {
			result = Math.max(result, d);
		}
		
		return result;
	}
	
	public static int max(int i1, int i2, int i3) {
		return Math.max(i1, Math.max(i2, i3));
	}
	
	public static int max(int i1, int i2, int i3, int i4) {
		return Math.max(Math.max(i1, i2), Math.max(i3, i4));
	}
	
	public static int max(int... list) {
		Preconditions.checkArgument(list.length>0);
		
		int result = list[0];
		for(int d : list) {
			result = Math.max(result, d);
		}
		
		return result;
	}
	
	
	
	
	public static int nextPowerOf2(int n) { 
		n--; 
		n |= n >> 1; 
		n |= n >> 2; 
		n |= n >> 4; 
		n |= n >> 8; 
		n |= n >> 16; 
		n++; 
		
		return n; 
	}
}
