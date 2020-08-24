package com.playsawdust.chipper.glow.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joml.Vector3d;
import org.joml.Vector3dc;

public class CollisionResult {
	private int voxelX;
	private int voxelY;
	private int voxelZ;
	
	private Vector3dc hitLocation;
	
	private ArrayList<Vector3dc> steps = new ArrayList<>();
	
	public Vector3d getVoxelCenter(Vector3d result) {
		if (result==null) result = new Vector3d();
		return result.set(voxelX+0.5, voxelY+0.5, voxelZ+0.5);
	}
	
	public Vector3dc getHitLocation() {
		return hitLocation;
	}
	
	public void setHitLocation(Vector3dc loc) {
		hitLocation = loc;
	}
	
	public void setVoxelPos(int x, int y, int z) {
		voxelX = x;
		voxelY = y;
		voxelZ = z;
	}
	
	public List<Vector3dc> getSteps() {
		return steps;
	}
	
	public void setSteps(Collection<Vector3dc> steps) {
		this.steps.clear();
		for(Vector3dc step : steps) {
			this.steps.add(step);
		}
	}
	
	public void clearSteps() {
		this.steps.clear();
	}
	
	public void addStep(Vector3dc step) {
		steps.add(step);
	}
}
