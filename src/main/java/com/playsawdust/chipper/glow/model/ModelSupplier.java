package com.playsawdust.chipper.glow.model;

import java.util.Iterator;

/**
 * Represents a Modifier (like Blender) or a dynamically-assembled model.
 */
public interface ModelSupplier {
	public Iterator<MeshSupplier> supplyMeshes();
}
