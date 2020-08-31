/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.model;

import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Vertex implements MaterialAttributeContainer {
	protected Vector3d pos; //TODO: Do pos and uv really need to be broken out? They can be stuffed into the attribute container now O_o
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getMaterialAttribute(MaterialAttribute<T> attribute) {
		if (attribute==MaterialAttribute.POSITION) return (T) pos;
		if (attribute==MaterialAttribute.UV) return (T) uv;
		return attributes.getMaterialAttribute(attribute);
	}
	
	@Override
	public <T> void putMaterialAttribute(MaterialAttribute<T> attribute, T value) {
		if (attribute==MaterialAttribute.POSITION) {
			pos.set((Vector3dc) value);
		} else if (attribute==MaterialAttribute.UV) {
			uv.set((Vector2dc) value);
		} else {
			attributes.putMaterialAttribute(attribute, value);
		}
	}
	
	@Override
	public <T> T removeMaterialAttribute(MaterialAttribute<T> attribute) {
		return attributes.removeMaterialAttribute(attribute);
	}
	
	@Override
	public void clearMaterialAttributes() {
		attributes.clearMaterialAttributes();
	}
	
	public Vertex copy() {
		Vertex result = new Vertex(this.pos, this.uv);
		result.attributes = this.attributes.copy();
		return result;
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