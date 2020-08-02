package com.playsawdust.chipper.glow.gl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.function.Function;

import org.joml.Vector3dc;

import com.playsawdust.chipper.glow.model.MaterialAttribute;

/**
 * Represents a generic VBO containing vertex *attribute* data.
 */
public class VertexBuffer {
	
	
	public static class Layout {
		private ArrayList<Entry<?>> entries = new ArrayList<>();
		
		public static class Entry<T> {
			MaterialAttribute<T> sourceData;
			int glDataClass;
			int glDataCount;
			String name;
			BufferWriter<T> writer;
			int destBytes = 0;
		}
	}
	
	private static interface BufferWriter<T> {
		public void write(ByteBuffer buf, T data);
	}
	
	private static BufferWriter<Vector3dc> makeVec3Writer(BufferWriter<Double> writer) {
		return (buf, vec) -> {
			writer.write(buf, vec.x());
			writer.write(buf, vec.y());
			writer.write(buf, vec.z());
		};
	}
	
	private static final int FP16_SIGN_SHIFT = 15;
	private static final int FP16_EXPONENT_SHIFT = 10;
	private static final int FP16_EXPONENT_BIAS = 15;
	private static final int FP32_SIGN_SHIFT = 31;
	private static final int FP32_EXPONENT_SHIFT = 23;
	private static final int FP32_EXPONENT_MASK = 0xff;
	private static final int FP32_SIGNIFICAND_MASK = 0x7fffff;
	private static final int FP32_EXPONENT_BIAS = 127;
	
	private static BufferWriter<Double> WRITE_DOUBLE_TO_HALF_FLOAT = (buf, it)->{
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
	private static BufferWriter<Double> WRITE_DOUBLE_TO_USHORT_NORMALIZED = (buf, it)->{
		double d = it.doubleValue()*Short.MAX_VALUE;
		buf.putShort((short)d);
	};
	private static BufferWriter<Double> WRITE_DOUBLE_TO_FLOAT = (buf, it)->{ buf.putFloat(it.floatValue()); };
	private static BufferWriter<Double> WRITE_DOUBLE_TO_DOUBLE = (buf, it)->{ buf.putDouble(it); };
	
	private static BufferWriter<Vector3dc> WRITE_VEC3_TO_HALF_FLOATS = makeVec3Writer(WRITE_DOUBLE_TO_HALF_FLOAT);
	private static BufferWriter<Vector3dc> WRITE_VEC3_TO_FLOATS = makeVec3Writer(WRITE_DOUBLE_TO_FLOAT);
	private static BufferWriter<Vector3dc> WRITE_VEC3_TO_DOUBLES = makeVec3Writer(WRITE_DOUBLE_TO_DOUBLE);
}
