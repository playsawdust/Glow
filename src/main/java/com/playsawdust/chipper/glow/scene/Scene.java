/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.scene;

import org.joml.FrustumIntersection;
import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.playsawdust.chipper.glow.RenderScheduler;
import com.playsawdust.chipper.glow.event.ConsumerEvent;
import com.playsawdust.chipper.glow.event.FixedTimestep;
import com.playsawdust.chipper.glow.event.Timestep;
import com.playsawdust.chipper.glow.gl.LightTexture;
import com.playsawdust.chipper.glow.gl.shader.ShaderProgram;
import com.playsawdust.chipper.glow.model.MaterialAttribute;
import com.playsawdust.chipper.glow.model.SimpleMaterialAttributeContainer;
import com.playsawdust.chipper.glow.pass.MeshPass;
import com.playsawdust.chipper.glow.pass.RenderPass;

public class Scene extends BoundingVolume {
	public static final Vector3dc WORLDSPACE_UP = new Vector3d(0, 1, 0);
	
	private Camera camera = new Camera();
	private Timestep timestep = FixedTimestep.ofTPS(20);
	
	private Matrix4d projectionMatrix = new Matrix4d();
	private SimpleMaterialAttributeContainer environment = new SimpleMaterialAttributeContainer();
	private LightTexture lights = new LightTexture();
	private Light sunLight = new Light();
	//private long globalStart = -1L;
	private ConsumerEvent<Integer> onTick = new ConsumerEvent<>();
	
	public Scene() {
		camera.collisionVolume = null;
		camera.setPosition(0,32,0);
		camera.clearLastPosition();
		camera.lookAt(32, 32, 32);
		
		environment.putMaterialAttribute(MaterialAttribute.AMBIENT_LIGHT, new Vector3d(0.05, 0.05, 0.1));
		//environment.putMaterialAttribute(MaterialAttribute.AMBIENT_LIGHT, new Vector3d(0.0, 0.0, 0.0));
		//environment.putMaterialAttribute(MaterialAttribute.DIFFUSE_COLOR, new Vector3d(1, 0, 0));
		
		//sunLight.setColor("#ffc");
		sunLight.setIntensity(1.0);
		sunLight.setPosition(32, 32, 32);
		sunLight.setRadius(100);
		lights.addLight(sunLight);
		
		//Light test = new Light();
		//test.setPosition(10, 8, 1);
		//test.setIntensity(0.6);
		//lights.addLight(test);
		
		timestep.onTick().register(this::onTickHandler);
	}
	
	public ConsumerEvent<Integer> onTick() {
		return onTick;
	}
	
	private void onTickHandler(int delta) {
		Vector3d pos = new Vector3d();
		camera.clearLastPosition();
		for(Light light : lights) {
			light.clearLastPosition();
		}
		for(Actor actor : this) {
			actor.clearLastPosition();
			//actor.setLastPosition(actor.getPosition(pos)); //Discard previous frame
			
			//TODO: move actors if they have velocity, collide them if they have colliders
		}
		
		//Fire the window onTick
		onTick.fire(delta);
	}
	
	public void setTimestep(Timestep timestep) {
		if (this.timestep!=null) {
			timestep.onTick().unregister(this::onTickHandler); //TODO: Make sure non-interned this::onTick == some other this::onTick; depends on lambdaMetafactory behavior
		}
		
		this.timestep = timestep;
		this.timestep.onTick().register(this::onTickHandler);
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
	
	//public long getElapsed() {
	//	return (System.nanoTime() / 1_000_000L) - globalStart;
	//}
	
	public void schedule(RenderScheduler scheduler) {
		schedule(scheduler, timestep.poll());
	}
	
	public void schedule(RenderScheduler scheduler, double tickProgress) {
		//double tickProgress = timestep.poll();
		
		lights.upload(tickProgress);
		
		//Get the rotated/translated view matrix
		Matrix4d viewMatrix = new Matrix4d(projectionMatrix);
		viewMatrix.mul(new Matrix4d(camera.getOrientation(null)));
		
		Vector3d cameraLast = camera.getLastPosition(null);
		Vector3d cameraCur = camera.getPosition(null);
		Vector3d cameraLerped = cameraLast.lerp(cameraCur, tickProgress); //overwrites cameraLast
		
		viewMatrix.translate(cameraLerped.mul(-1));
		Matrix4f viewFloats = new Matrix4f(viewMatrix);
		
		FrustumIntersection frustumTest = new FrustumIntersection(viewFloats);
		//Reuse vectors to cut down on eden pressure
		Vector3d collisionCenter = new Vector3d();
		Vector3d last = new Vector3d();
		Vector3d cur = new Vector3d();
		double collisionRadius = 0.0;
		
		for(Actor actor : this) {
			//Get current position
			actor.getLastPosition(last);
			actor.getPosition(cur);
			Vector3d lerped = last.lerp(cur, tickProgress);
			
			//Frustum Cull
			boolean intersects = true;
			CollisionVolume collision = actor.getCollision();
			
			if (collision!=null) {
				//18mul + 18add for sphere-frustum
				//18mul + 12add + 18 conditional moves + 6 compares
				//So we go with spheres for coarse passes, always
				collision.getSphereOffset(collisionCenter);
				collisionCenter = collisionCenter.add(lerped);
				collisionRadius = collision.getSphereRadius();
				
				intersects = frustumTest.testSphere((float) collisionCenter.x, (float) collisionCenter.y, (float) collisionCenter.z, (float) collisionRadius);
			}
			
			if (intersects) {
			
				Object renderObject = actor.getRenderObject(camera);
				if (renderObject==null) continue;
				Matrix3d orientation = actor.getOrientation(null);
				scheduler.schedule(renderObject, lerped, orientation, environment);
			
			}
		}
		
		//We can maybe resolve these by scheduling the light texture on the scheduler and letting it mutate the program
		RenderPass solidPass = scheduler.getPass("solid"); //TODO: bad assumptions about pass name
		if (solidPass instanceof MeshPass) {
			ShaderProgram solidShader = ((MeshPass) solidPass).getProgram();
			solidShader.bind();
			//lights.upload();
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

	public Timestep getTimestep() {
		return timestep;
	}
}
