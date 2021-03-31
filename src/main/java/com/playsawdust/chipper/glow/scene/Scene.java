/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.scene;

import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.playsawdust.chipper.glow.RenderScheduler;
import com.playsawdust.chipper.glow.gl.LightTexture;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.SimpleMaterialAttributeContainer;
import com.playsawdust.chipper.glow.pass.MeshPass;
import com.playsawdust.chipper.glow.pass.RenderPass;

public class Scene extends BoundingVolume {
	public static final Vector3dc WORLDSPACE_UP = new Vector3d(0, 1, 0);
	
	private Camera camera = new Camera();
	private Matrix4d projectionMatrix = new Matrix4d();
	private SimpleMaterialAttributeContainer environment = new SimpleMaterialAttributeContainer();
	private LightTexture lights = new LightTexture();
	private Light sunLight = new Light();
	private long globalStart = -1L;
	
	public Scene() {
		camera.collisionVolume = null;
		camera.setPosition(0,32,0);
		camera.lookAt(32, 32, 32);
		
		environment.putMaterialAttribute(MaterialAttribute.AMBIENT_LIGHT, new Vector3d(0.40, 0.40, 0.60));
		
		//environment.putMaterialAttribute(MaterialAttribute.AMBIENT_LIGHT, new Vector3d(0.00, 0.00, 0.00));
		
		
		sunLight.setColor("#ffc");
		sunLight.setIntensity(1.0);
		sunLight.setPosition(32, 32, 32);
		sunLight.setRadius(100);
		lights.addLight(sunLight);
		
		Light test = new Light();
		test.setPosition(10, 8, 1);
		test.setIntensity(0.6);
		//lights.addLight(test);
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public void setProjectionMatrix(Matrix4dc projection) {
		this.projectionMatrix.set(projection);
	}
	
	public Matrix4dc getProjectionMatrix() {
		return this.projectionMatrix;
	}
	
	public long getElapsed() {
		return (System.nanoTime() / 1_000_000L) - globalStart;
	}
	
	public void schedule(RenderScheduler scheduler) {
		if (globalStart==-1) globalStart = System.nanoTime() / 1_000_000L;
		long globalElapsed = getElapsed();
		
		
		
		sunLight.setPosition(128, 128, 256+Math.sin(globalElapsed/5_000.0)*256);
		
		for(Actor actor : this) {
			Object renderObject = actor.getRenderObject(camera);
			if (renderObject==null) continue;
			Vector3d pos = actor.getPosition(null);
			Matrix3d orientation = actor.getOrientation(null);
			scheduler.schedule(renderObject, pos, orientation, environment);
		}
		
		//We can maybe resolve these by scheduling the light texture on the scheduler and letting it mutate the program
		RenderPass solidPass = scheduler.getPass("solid"); //TODO: bad assumptions about pass name
		if (solidPass instanceof MeshPass) {
			ShaderProgram solidShader = ((MeshPass) solidPass).getProgram();
			solidShader.bind();
			lights.upload();
			lights.bind(solidShader, 1); //TODO: bad assumptions about texture unit
		}
		
		/*
		Matrix4d viewMatrix = new Matrix4d(projectionMatrix);
		viewMatrix.mul(new Matrix4d(camera.getOrientation(null)));
		viewMatrix.translate(camera.getPosition(null).mul(-1));
		scheduler.render(viewMatrix);*/
	}
	
	public Light getSun() {
		return sunLight;
	}
}
