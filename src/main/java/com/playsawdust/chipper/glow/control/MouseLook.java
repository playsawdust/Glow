package com.playsawdust.chipper.glow.control;

import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Vector3d;

public class MouseLook {
	private static final double QUARTER_TAU = Math.PI/2;
	
	private double lastX = Double.NaN;
	private double lastY = Double.NaN;
	
	private double xRot = 0;
	private double yRot = 0;
	private double xSensitivity = 0.001; //TODO: Relative to window dimensions?
	private double ySensitivity = 0.001;
	private Matrix3d matrix = new Matrix3d();
	
	public Matrix3dc step(double mouseX, double mouseY, double winWidth, double winHeight) {
		if (Double.isNaN(lastX)) lastX = mouseX;
		if (Double.isNaN(lastY)) lastY = mouseY;
		double dx = mouseX - lastX;
		double dy = mouseY - lastY;
		lastX = mouseX;
		lastY = mouseY;
		
		yRot += dx * xSensitivity;
		xRot += dy * ySensitivity;
		
		if (xRot >  QUARTER_TAU) xRot =  QUARTER_TAU;
		if (xRot < -QUARTER_TAU) xRot = -QUARTER_TAU;
		
		matrix.identity();
		matrix.rotate(xRot, 1, 0, 0);
		matrix.rotate(yRot, 0, 1, 0);
		
		return matrix;
	}
	
	public Matrix3dc getMatrix() {
		return matrix;
	}
	
	public Vector3d getLookVector(Vector3d result) {
		if (result==null) result = new Vector3d();
		
		result.set(0, 0, -1);
		new Matrix3d(matrix).invert().transform(result);
		return result;
	}
	
	public Vector3d getRightVector(Vector3d result) {
		if (result==null) result = new Vector3d();
		
		result.set(1, 0, 0);
		new Matrix3d(matrix).invert().transform(result);
		return result;
	}
}
