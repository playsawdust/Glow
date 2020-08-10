package com.playsawdust.chipper.glow.gl;

import java.nio.ByteBuffer;

import org.joml.Vector3dc;

public interface BufferWriter<T> {
	static final int UBYTE_MAX = 0xFF;
	static final int USHORT_MAX = 0xFFFF;
	
	
	public void write(ByteBuffer buf, T data);
	
	/**
	 * Cast an Object to the type written by this BufferWriter, and write it to the buffer. Often the circumstances
	 * of writing a buffer mean that 
	 * @param buf
	 * @param data
	 */
	@SuppressWarnings("unchecked")
	public default void writeUnsafe(ByteBuffer buf, Object data) {
		write(buf, (T)data);
	}
	
	public static BufferWriter<Vector3dc> makeVec3Writer(BufferWriter<Double> writer) {
		return (buf, vec) -> {
			writer.write(buf, vec.x());
			writer.write(buf, vec.y());
			writer.write(buf, vec.z());
		};
	}
	
	
	public static BufferWriter<Integer> WRITE_INT_TO_INT = (buf, it) -> { buf.putInt(it); };
	
	
	
	public static BufferWriter<Double> WRITE_DOUBLE_TO_HALF_FLOAT = (buf, it)->{
		int FP16_SIGN_SHIFT = 15;
		int FP16_EXPONENT_SHIFT = 10;
		int FP16_EXPONENT_BIAS = 15;
		int FP32_SIGN_SHIFT = 31;
		int FP32_EXPONENT_SHIFT = 23;
		int FP32_EXPONENT_MASK = 0xff;
		int FP32_SIGNIFICAND_MASK = 0x7fffff;
		int FP32_EXPONENT_BIAS = 127;
		
		int bits = Float.floatToRawIntBits((float)it.doubleValue());
		int s = (bits >>> FP32_SIGN_SHIFT);
		int e = (bits >>> FP32_EXPONENT_SHIFT) & FP32_EXPONENT_MASK;
		int m = (bits) & FP32_SIGNIFICAND_MASK;

		int outE = 0;
		int outM = 0;

		if (e == 0xff) { // Infinite or NaN
			outE = 0x1f;
			outM = m != 0 ? 0x200 : 0;
		} else {
			e = e - FP32_EXPONENT_BIAS + FP16_EXPONENT_BIAS;
			if (e >= 0x1f) { // Overflow
				outE = 0x31;
			} else if (e <= 0) { // Underflow
				if (e < -10) {
					// The absolute fp32 value is less than MIN_VALUE, flush to +/-0
				} else {
					// The fp32 value is a normalized float less than MIN_NORMAL,
					// we convert to a denorm fp16
					m = (m | 0x800000) >> (1 - e);
					if ((m & 0x1000) != 0)
						m += 0x2000;
					outM = m >> 13;
				}
			} else {
				outE = e;
				outM = m >> 13;
				if ((m & 0x1000) != 0) {
					// Round to nearest "0.5" up
					int out = (outE << FP16_EXPONENT_SHIFT) | outM;
					out++;
					
					short result = (short) (out | (s << FP16_SIGN_SHIFT));
					buf.putShort(result);
					return;
				}
			}
		}
		
		short result = (short) ((s << FP16_SIGN_SHIFT) | (outE << FP16_EXPONENT_SHIFT) | outM);
		buf.putShort(result);
	};
	
	/* A note on writing doubles to normalized integers:
	 * 
	 * per https://www.khronos.org/opengl/wiki/Normalized_Integer#Signed
	 * signed normalized integers are unpacked with effectively
	 * 
	 * <code>
	 * 
	 *   Math.max((double)it/MAX_VALUE, 1.0)
	 * 
	 * </code>
	 * 
	 * which crushes both MIN_VALUE and MIN_VALUE+1 against -1
	 * The nice thing about this is that it keeps zero, *at zero*. And the error at the end is 1/MAX_VALUE th
	 * 
	 * So rather than stretching negative numbers to reach the single extra value, we do a straightforward
	 * inverse transform which gives us values in the range [ MIN_VALUE+1 .. MAX_VALUE ]. And trust that
	 * the MIN_VALUE+1 that -1 maps to will get properly unmapped back to -1:
	 * 
	 * <code>
	 * 
	 *   it*MAX_VALUE
	 * 
	 * </code>
	 */
	
	public static BufferWriter<Double> WRITE_DOUBLE_TO_BYTE_NORMALIZED = (buf, it)->{
		double d = it.doubleValue()*Byte.MAX_VALUE;
		buf.put((byte)d);
	};
	
	public static BufferWriter<Double> WRITE_DOUBLE_TO_UBYTE_NORMALIZED = (buf, it)->{
		buf.putShort((short)(it*UBYTE_MAX));
	};
	public static BufferWriter<Double> WRITE_DOUBLE_TO_SHORT_NORMALIZED = (buf, it)->{
		double d = it.doubleValue()*Short.MAX_VALUE;
		buf.putShort((short)d);
	};
	public static BufferWriter<Double> WRITE_DOUBLE_TO_USHORT_NORMALIZED = (buf, it)->{
		buf.putShort((short)(it*USHORT_MAX));
	};
	
	public static BufferWriter<Double> WRITE_DOUBLE_TO_BYTE = (buf, it)->{ buf.putInt((byte)it.intValue()); };
	public static BufferWriter<Double> WRITE_DOUBLE_TO_SHORT = (buf, it)->{ buf.putShort((short)it.intValue()); };
	public static BufferWriter<Double> WRITE_DOUBLE_TO_INT = (buf, it)->{ buf.putInt(it.intValue()); };
	public static BufferWriter<Double> WRITE_DOUBLE_TO_FLOAT = (buf, it)->{ buf.putFloat(it.floatValue()); };
	public static BufferWriter<Double> WRITE_DOUBLE_TO_DOUBLE = (buf, it)->{ buf.putDouble(it); };
	
	
	public static BufferWriter<Vector3dc> WRITE_VEC3_TO_HALF_FLOATS = makeVec3Writer(WRITE_DOUBLE_TO_HALF_FLOAT);
	public static BufferWriter<Vector3dc> WRITE_VEC3_TO_FLOATS = makeVec3Writer(WRITE_DOUBLE_TO_FLOAT);
	public static BufferWriter<Vector3dc> WRITE_VEC3_TO_DOUBLES = makeVec3Writer(WRITE_DOUBLE_TO_DOUBLE);
}
