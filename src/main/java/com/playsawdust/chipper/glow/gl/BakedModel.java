package com.playsawdust.chipper.glow.gl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.playsawdust.chipper.glow.gl.shader.Destroyable;

public class BakedModel implements Iterable<BakedMesh>, Destroyable {
	private ArrayList<BakedMesh> meshes = new ArrayList<>();
	
	public BakedModel(Collection<BakedMesh> meshes) {
		for(BakedMesh mesh : meshes) this.meshes.add(mesh);
	}
	
	@Override
	public void destroy() {
		for(BakedMesh mesh : meshes) {
			mesh.destroy();
		}
		
		meshes.clear();
	}

	@Override
	public Iterator<BakedMesh> iterator() {
		return meshes.iterator();
	}
}
