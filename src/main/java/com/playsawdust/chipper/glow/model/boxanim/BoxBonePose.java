/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.model.boxanim;

import org.joml.Matrix4d;
import org.joml.Vector3d;

public class BoxBonePose {
	private String name;
	private Vector3d pivotPoint = new Vector3d(0,0,0);
	
	private Vector3d rotation = new Vector3d(0,0,0);
	private Vector3d position = new Vector3d(0,0,0);
	private Vector3d scale = new Vector3d(0,0,0);
	
	public Matrix4d getMatrix(Matrix4d result) {
		if (result==null) result = new Matrix4d();
		//TODO: This order is probably reversed
		result.setTranslation(pivotPoint.negate(new Vector3d()));
		result.rotateXYZ(rotation);
		result.setTranslation(position.x+pivotPoint.x, position.y+pivotPoint.y, position.z+pivotPoint.z);
		result.scale(scale);
		return result;
	}
}
