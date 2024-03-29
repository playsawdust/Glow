/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.scene;

import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Light extends Actor {
	private Vector3d direction = new Vector3d(0, 0, 0);
	private Vector3d color = new Vector3d(1, 1, 1);
	private double radius = 16.0;
	private double angle = 2.0;
	private double intensity = 1.0;
	
	public Vector3dc getDirection() {
		return direction;
	}
	
	public Vector3dc getColor() {
		return color;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public double getIntensity() {
		return intensity;
	}
	
	public double getAngle() {
		return angle;
	}

	public void setColor(Vector3dc color) {
		this.color.set(color);
	}
	
	private static int hexDigit(char ch) {
		switch(ch) {
		default:
		case '0': return 0;
		case '1': return 1;
		case '2': return 2;
		case '3': return 3;
		case '4': return 4;
		case '5': return 5;
		case '6': return 6;
		case '7': return 7;
		case '8': return 8;
		case '9': return 9;
		case 'A':
		case 'a': return 10;
		case 'B':
		case 'b': return 11;
		case 'C':
		case 'c': return 12;
		case 'D':
		case 'd': return 13;
		case 'E':
		case 'e': return 14;
		case 'F':
		case 'f': return 15;
		}
	}
	
	public void setColor(String hex) {
		int start = (hex.startsWith("#")) ? 1 : 0;
		if (hex.length()==start+3) {
			int r = hexDigit(hex.charAt(start+0));
			int g = hexDigit(hex.charAt(start+1));
			int b = hexDigit(hex.charAt(start+2));
			r += r << 4;
			g += g << 4;
			b += b << 4;
			this.color.set((double)r/255.0, (double)g/255.0, (double)b/255.0);
		} else if (hex.length()==start+6) {
			int r = hexDigit(hex.charAt(start+0)) << 4;
			r += hexDigit(hex.charAt(start+1));
			
			int g = hexDigit(hex.charAt(start+2)) << 4;
			g += hexDigit(hex.charAt(start+3));
			
			int b = hexDigit(hex.charAt(start+4)) << 4;
			b += hexDigit(hex.charAt(start+5));
			
			this.color.set(r/255.0, g/255.0, b/255.0);
		} else throw new IllegalArgumentException("Don't know how to process a color with "+(hex.length()-start)+" characters.");
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}
}
