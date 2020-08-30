package com.playsawdust.chipper.glow.model.boxanim;

import java.util.ArrayList;

import org.joml.Matrix3d;

import com.playsawdust.chipper.glow.model.Model;

public class BoxBone {
	private String name;
	private ArrayList<Model> models = new ArrayList<>();
	private Matrix3d basePose;
	
	public BoxBone(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
