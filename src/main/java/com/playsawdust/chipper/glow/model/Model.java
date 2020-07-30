package com.playsawdust.chipper.glow.model;

public class Model {
	public static class Submodel {
		private Material material;
		private EditableMesh mesh;
		
		public Submodel(Material mat, EditableMesh mesh) {
			this.material = mat;
			this.mesh = mesh;
		}
	}
}
