package com.playsawdust.chipper.glow.util;

public class RandomAccessDataInput {
	protected int base = 0;
	protected int limit;
	protected int offset = 0;
	protected byte[] data;
	
	public RandomAccessDataInput(byte[] data) {
		this.data = data;
		this.limit = data.length;
	}
	
	public void seek(int offset) {
		checkIndex(offset);
		this.offset = offset;
	}
	
	public int position() {
		return this.offset;
	}
	
	/** UNTESTED */
	public RandomAccessDataInput slice(int length) {
		if (length>this.limit-offset) throw new IllegalArgumentException("Not enough data left to make this slice: slice length "+length+", available "+(limit-offset));
		
		RandomAccessDataInput result = new RandomAccessDataInput(data);
		result.base = this.offset;
		result.limit = length;
		return result;
	}
	
	public byte readByte() {
		checkIndex(offset);
		byte result = data[base+offset];
		offset++;
		return result;
	}
	
	public int readUByte() {
		checkIndex(offset);
		int result = data[base+offset] & 0xFF;
		offset++;
		return result;
	}
	
	public int readInt16() {
		int hi = readUByte();
		int lo = readUByte();
		
		return (short) (hi << 8 | lo);
	}
	
	public int readUInt16() {
		int hi = readUByte();
		int lo = readUByte();
		return hi << 8 | lo;
	}
	
	public int readInt32() {
		int a = readUByte();
		int b = readUByte();
		int c = readUByte();
		int d = readUByte();
		
		return a << 24 | b << 16 | c << 8 | d;
	}
	
	public long readUInt32() {
		long a = readUByte();
		long b = readUByte();
		long c = readUByte();
		long d = readUByte();
		
		return a << 24 | b << 16 | c << 8 | d;
	}
	
	public long readInt64() {
		long a = readUByte();
		long b = readUByte();
		long c = readUByte();
		long d = readUByte();
		
		long e = readUByte();
		long f = readUByte();
		long g = readUByte();
		long h = readUByte();
		
		return 
			a << 56 |
			b << 48 |
			c << 40 |
			d << 32 |
			e << 24 |
			f << 16 |
			g <<  8 |
			h;
	}
	
	/** Convenience method which reads a UInt32, but if the result makes use of the top bit, clamps the value to Integer.MAX_VALUE. */
	public int readUInt32Clamped() {
		long result = readUInt32();
		if (result>Integer.MAX_VALUE) return Integer.MAX_VALUE;
		else return (int)result;
	}
	
	/* I would use Preconditions, but it's 0..size *inclusive* which is unhelpful */
	private void checkIndex(int index) {
		if (index < 0 || index >= limit) throw new ArrayIndexOutOfBoundsException("Index was "+index+", bounds were [0.."+limit+")");
	}
}
