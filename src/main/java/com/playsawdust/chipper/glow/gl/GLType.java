package com.playsawdust.chipper.glow.gl;

import org.lwjgl.opengl.GL20;

/** Exhaustive enum of all types available to &lt;= GL20 */
public enum GLType {
	BOOLEAN(GL20.GL_BOOL),
	VEC2B(GL20.GL_BOOL_VEC2),
	VEC3B(GL20.GL_BOOL_VEC3),
	VEC4B(GL20.GL_BOOL_VEC4),
	
	BYTE(GL20.GL_BYTE),
	UBYTE(GL20.GL_UNSIGNED_BYTE),
	TWO_BYTES(GL20.GL_2_BYTES),
	THREE_BYTES(GL20.GL_3_BYTES),
	FOUR_BTYES(GL20.GL_4_BYTES),
	
	SHORT(GL20.GL_SHORT),
	USHORT(GL20.GL_UNSIGNED_SHORT),
	
	INT(GL20.GL_INT),
	UINT(GL20.GL_UNSIGNED_INT),
	VEC2I(GL20.GL_INT_VEC2),
	VEC3I(GL20.GL_INT_VEC3),
	VEC4I(GL20.GL_INT_VEC4),
	
	FLOAT(GL20.GL_FLOAT),
	VEC2F(GL20.GL_FLOAT_VEC2),
	VEC3F(GL20.GL_FLOAT_VEC3),
	VEC4F(GL20.GL_FLOAT_VEC4),
	MAT2X2F(GL20.GL_FLOAT_MAT2),
	MAT3X3F(GL20.GL_FLOAT_MAT3),
	MAT4X4F(GL20.GL_FLOAT_MAT4),
	
	DOUBLE(GL20.GL_DOUBLE),
	
	UNKNOWN(-1);
	
	private int glConstant;
	
	GLType(int glConstant) {
		this.glConstant = glConstant;
	}
	
	public static GLType of(int i) {
		for(GLType t : values()) {
			if (t.glConstant == i) return t;
		}
		
		return UNKNOWN;
	}
	
}