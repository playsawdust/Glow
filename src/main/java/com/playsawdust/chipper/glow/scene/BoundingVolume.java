package com.playsawdust.chipper.glow.scene;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

public class BoundingVolume implements Iterable<Actor> {
	private boolean enabled = true;
	private ArrayList<BoundingVolume> subVolumes = new ArrayList<>();
	private ArrayList<Actor> actors = new ArrayList<>();
	
	public ImmutableList<BoundingVolume> boundingVolumes() {
		return ImmutableList.copyOf(subVolumes);
	}
	
	public void addVolume(BoundingVolume volume) {
		subVolumes.add(volume);
	}
	
	public void addActor(Actor actor) {
		actors.add(actor);
	}
	
	public void removeVolume(BoundingVolume volume) {
		subVolumes.remove(volume);
	}
	
	public void removeActor(Actor actor) {
		actors.remove(actor);
	}
	
	@Override
	public Iterator<Actor> iterator() {
		Iterator<Actor> result = actors.iterator();
		for(BoundingVolume volume : subVolumes) {
			if (volume.enabled) result = Iterators.concat(result, volume.iterator());
		}
		
		return result;
	}
}
