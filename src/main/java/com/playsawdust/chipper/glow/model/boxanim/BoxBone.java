package com.playsawdust.chipper.glow.model.boxanim;

import org.joml.Matrix3d;

import com.playsawdust.chipper.glow.model.Model;

public class BoxBone {
	private String name;
	private Model model;
	private Matrix3d basePose;
	
	public BoxBone(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}
}
