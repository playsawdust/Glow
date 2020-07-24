package com.playsawdust.chipper.glow.model;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface Material {
	
	/**
	 * Gets an attribute of this Material
	 * @param <T> The immutable type of the data represented by this MaterialAttribute
	 * @param attribute the MaterialAttribute to return a value for
	 * @return the value of this MaterialAttribute, or null if no value is defined for this Material.
	 */
	public <T,U> @Nullable T getMaterialAttribute(MaterialAttribute<T, U> attribute);
	
	/**
	 * Gets the value of a MaterialAttribute for this Material. For instance, getting its diffuse color as a Vec3
	 * @param <U> The mutable type of data represented by this MaterialAttribute
	 * @param attribute the MaterialAttribute to return a value for
	 * @param result the object to place the value of this MaterialAttribute into; the object will be unchanged if no value is defined for this Material.
	 */
	public <T,U> void getMaterialAttribute(MaterialAttribute<T, U> attribute, U result);
}
