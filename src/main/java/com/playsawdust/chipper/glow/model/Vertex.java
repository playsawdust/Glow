package com.playsawdust.chipper.glow.model;

import org.joml.Vector2d;
import org.joml.Vector3d;

public class Vertex implements MaterialAttributeContainer, MaterialAttributeDelegateHolder {
	protected Vector3d pos;
	protected Vector2d uv;
	protected SimpleMaterialAttributeContainer attributes = new SimpleMaterialAttributeContainer();
	
	public Vertex(Vector3d pos) {
		this.pos = new Vector3d(pos);
		this.uv = new Vector2d(0, 0);
	}

	public Vertex(Vector3d pos, Vector2d uv) {
		this.pos = new Vector3d(pos);
		this.uv = new Vector2d(uv);
	}

	@Override
	public MaterialAttributeContainer getDelegate() {
		return attributes;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof Vertex)) return false;
		Vertex that = (Vertex)o;
		
		return
				(that.pos.equals(this.pos)) &&
				(that.uv.equals(this.uv)) &&
				(that.attributes.equals(this.attributes));
	}
		
	@Override
	public int hashCode() {
		int result = 1;

		result *= 31;
		result += pos.hashCode();
		
		result *= 31;
		result += uv.hashCode();
		
		result *= 31;
		result += attributes.hashCode();
		
		return result;
	}
}