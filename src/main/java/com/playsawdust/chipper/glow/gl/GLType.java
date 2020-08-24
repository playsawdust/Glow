package com.playsawdust.chipper.glow.gl;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.AMDGPUShaderInt64;
import org.lwjgl.opengl.ARBTextureRectangle;

/** Exhaustive enum of all types available to &lt;= GL20 */
public enum GLType {
	/** In OpenGL, booleans are the only type without a defined byte size.
	 * <p>Not allowed for vertex attribute data. */
	BOOLEAN(GL20.GL_BOOL),
	/** Not allowed for vertex attribute data. */
	VEC2_BOOLEAN(GL20.GL_BOOL_VEC2),
	/** Not allowed for vertex attribute data. */
	VEC3_BOOLEAN(GL20.GL_BOOL_VEC3),
	/** Not allowed for vertex attribute data. */
	VEC4_BOOLEAN(GL20.GL_BOOL_VEC4),
	
	BYTE(GL20.GL_BYTE, 1),
	UBYTE(GL20.GL_UNSIGNED_BYTE, 1),
	
	SHORT(GL20.GL_SHORT, 2),
	/** Not allowed for vertex attribute data. */
	VEC2_SHORT(AMDGPUShaderInt64.GL_INT16_VEC2_NV),
	/** Not allowed for vertex attribute data. */
	VEC3_SHORT(AMDGPUShaderInt64.GL_INT16_VEC3_NV),
	/** Not allowed for vertex attribute data. */
	VEC4_SHORT(AMDGPUShaderInt64.GL_INT16_VEC4_NV),
	USHORT(GL20.GL_UNSIGNED_SHORT, 2),
	
	INT(GL20.GL_INT, 4),
	/** Not allowed for vertex attribute data. */
	VEC2I(GL20.GL_INT_VEC2),
	/** Not allowed for vertex attribute data. */
	VEC3I(GL20.GL_INT_VEC3),
	/** Not allowed for vertex attribute data. */
	VEC4I(GL20.GL_INT_VEC4),
	UINT(GL20.GL_UNSIGNED_INT, 4),
	
	/** Not allowed for vertexAttribPointer - this is a strange extension and it's not clear whether this can be
	 * specified in a VBO at all, and if so, whether it's vertexAttribIPointer or vertexAttribLPointer. */
	INT64(AMDGPUShaderInt64.GL_INT64_NV, 8),
	/** Not allowed for vertex attribute data. */
	VEC2L(AMDGPUShaderInt64.GL_INT64_VEC2_NV),
	/** Not allowed for vertex attribute data. */
	VEC3L(AMDGPUShaderInt64.GL_INT64_VEC3_NV),
	/** Not allowed for vertex attribute data. */
	VEC4L(AMDGPUShaderInt64.GL_INT64_VEC4_NV),
	/** Not allowed for vertexAttribPointer - this is a strange extension and it's not clear whether this can be
	 * specified in a VBO at all, and if so, whether it's vertexAttribIPointer or vertexAttribLPointer. */
	UINT64(AMDGPUShaderInt64.GL_UNSIGNED_INT64_NV, 8),
	
	HALF_FLOAT(GL30.GL_HALF_FLOAT, 2),
	
	FLOAT(GL20.GL_FLOAT, 4),
	/** Not allowed for vertex attribute data. */
	VEC2F(GL20.GL_FLOAT_VEC2),
	/** Not allowed for vertex attribute data. */
	VEC3F(GL20.GL_FLOAT_VEC3),
	/** Not allowed for vertex attribute data. */
	VEC4F(GL20.GL_FLOAT_VEC4),
	/** Not allowed for vertex attribute data. */
	MAT2F(GL20.GL_FLOAT_MAT2),
	/** Not allowed for vertex attribute data. */
	MAT3F(GL20.GL_FLOAT_MAT3),
	/** Not allowed for vertex attribute data. */
	MAT4F(GL20.GL_FLOAT_MAT4),
	
	/** Not allowed for vertexAttribPointer - vertex data needs to be described specially via VertexAttribLPointer. */
	DOUBLE(GL20.GL_DOUBLE, 8),
	/** Not allowed for vertex attribute data. */
	VEC2D(GL40.GL_DOUBLE_VEC2),
	/** Not allowed for vertex attribute data. */
	VEC3D(GL40.GL_DOUBLE_VEC3),
	/** Not allowed for vertex attribute data. */
	VEC4D(GL40.GL_DOUBLE_VEC4),
	/** Not allowed for vertex attribute data. */
	MAT2D(GL40.GL_DOUBLE_MAT2),
	/** Not allowed for vertex attribute data. */
	MAT3D(GL40.GL_DOUBLE_MAT3),
	/** Not allowed for vertex attribute data. */
	MAT4D(GL40.GL_DOUBLE_MAT4),
	
	/** Not allowed for vertex attribute data. */
	SAMPLER2D(GL20.GL_SAMPLER_2D),
	/** Not allowed for vertex attribute data. */
	SAMPLER3D(GL20.GL_SAMPLER_3D),
	
	SAMPLER2D_RECT(ARBTextureRectangle.GL_SAMPLER_2D_RECT_ARB),
	
	//reserved by library
	UNKNOWN(-1);
	
	private int glConstant;
	private int size = -1;
	
	GLType(int glConstant) {
		this.glConstant = glConstant;
	}
	
	GLType(int glConstant, int size) {
		this.glConstant = glConstant;
		this.size = size;
	}
	
	/** Returns the size in bytes of VBO data of this openGL type. If the type isn't allowed for VBOs, or the size is unknown, -1 is returned. */
	public int getSize() {
		return size;
	}
	
	public static GLType of(int i) {
		for(GLType t : values()) {
			if (t.glConstant == i) return t;
		}
		
		return UNKNOWN;
	}
	
}