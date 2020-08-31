package com.playsawdust.chipper.glow.model.boxanim;

import java.util.ArrayList;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;

public class BoxModel {
	private ArrayList<BoxBone> bones = new ArrayList<>();
	
	public Model createModel(@Nullable BoxPose pose) {
		Model result = new Model();
		for(BoxBone bone : bones) {
			System.out.println("Combining from "+bone.getName()+"("+bone.getModel().getMeshCount()+" meshes)");
			if (!bone.getModel().isEmpty()) {
				Mesh mesh = bone.getModel().getMesh(0);
				System.out.println("    ("+mesh.getFaceCount()+" faces)");
			}
			result.combineFrom(bone.getModel()); //TODO: Pose-transform
		}
		
		if (!result.isEmpty()) {
			System.out.println(result.getMeshCount()+" meshes in final model.");
			for(Mesh m : result) {
				System.out.println("    "+m.getFaceCount()+" faces");
			}
		} else {
			System.out.println("No meshes in result!");
		}
		
		return result;
	}

	public void addBone(BoxBone bone) {
		bones.add(bone);
	}
}
