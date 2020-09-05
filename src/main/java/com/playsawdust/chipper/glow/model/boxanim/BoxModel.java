/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.model.boxanim;

import java.util.ArrayList;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.playsawdust.chipper.glow.model.MeshSupplier;
import com.playsawdust.chipper.glow.model.Model;

public class BoxModel {
	private ArrayList<BoxBone> bones = new ArrayList<>();
	
	public Model createModel(@Nullable BoxPose pose) {
		Model result = new Model();
		for(BoxBone bone : bones) {
			System.out.println("Combining from "+bone.getName()+"("+bone.getModel().getMeshCount()+" meshes)");
			if (!bone.getModel().isEmpty()) {
				MeshSupplier mesh = bone.getModel().getMesh(0);
				System.out.println("    ("+mesh.supplyMesh().getFaceCount()+" faces)");
			}
			result.combineFrom(bone.getModel()); //TODO: Pose-transform
		}
		
		if (!result.isEmpty()) {
			System.out.println(result.getMeshCount()+" meshes in final model.");
			for(MeshSupplier m : result) {
				System.out.println("    "+m.supplyMesh().getFaceCount()+" faces");
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
