package com.playsawdust.chipper.glow.pass;

import java.util.ArrayList;

import org.joml.Vector3dc;

import com.playsawdust.chipper.glow.model.Mesh;
import com.playsawdust.chipper.glow.model.Model;

public class MeshPass implements RenderPass {
	
	private ArrayList<Entry> scheduled = new ArrayList<>();
	
	@Override
	public void apply() {
		for(Entry entry : scheduled) {
			Mesh mesh = entry.mesh;
			for(Mesh.Face face : mesh.faces()) {
				
			}
		}
		
		scheduled.clear();
	}

	@Override
	public boolean canEnqueue(Object o) {
		return (o instanceof Model) || (o instanceof Mesh); //&& matchesOtherOptions, e.g. shader, opacity
	}

	@Override
	public void enqueue(Object o, Vector3dc position) {
		if (o instanceof Model) {
			//Break into meshes
		} else if (o instanceof Mesh) {
			FlatMesh cacheData = ((Mesh)o).getCache(this, FlatMesh.class);
			
			if (cacheData==null) {
				cacheData = new FlatMesh();
				cacheData.mesh = (Mesh)o; //For now, we don't bother to flatten
			}
			
			scheduled.add(new Entry((Mesh)o, position));
		}
	}
	
	private static class FlatMesh {
		Vector3dc position;
		byte[] data;
		Mesh mesh;
	}
	
	private static class Entry {
		Mesh mesh; //TODO: Flattened data instead of mesh
		Vector3dc position;
		
		public Entry(Mesh mesh, Vector3dc position) {
			this.mesh = mesh;
			this.position = position;
		}
	}
}
