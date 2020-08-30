package com.playsawdust.chipper.glow.model.boxanim;

import java.util.ArrayList;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.playsawdust.chipper.glow.model.Model;

public class BoxModel {
	private ArrayList<BoxBone> bones = new ArrayList<>();
	
	public Model createModel(@Nullable BoxPose pose) {
		Model result = new Model();
		
		return result;
	}
}
