package com.playsawdust.chipper.glow.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class holds material surface properties.
 */
public class SimpleMaterialAttributeContainer implements MaterialAttributeContainer {
	private HashMap<MaterialAttribute<?>, Object> attributes = new HashMap<>();
	
	/**
	 * Gets a MaterialAttribute setting from this container.
	 * @param <T> The value type for this attribute
	 * @param attribute The MaterialAttribute key to get a value for
	 * @return The value mapped to the provided attribute, or null if no value is mapped.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> @Nullable T getMaterialAttribute(MaterialAttribute<T> attribute) {
		return (T) attributes.get(attribute);
	}
	
	/**
	 * Adds a MaterialAttribute setting to this container.
	 * @param <T> The value type for this attribute
	 * @param attribute The MaterialAttribute key to set a value for
	 * @param t The value to map this MaterialAttribute to for this container
	 */
	@Override
	public <T> void putMaterialAttribute(MaterialAttribute<T> attribute, @NonNull T t) {
		attributes.put(attribute, t);
	}
	
	/**
	 * Clears a MaterialAttribute setting from this container. When this call is complete, whether or not there
	 * was originally a setting for this attribute, getMaterialAttribute will return null for this attribute.
	 * @param attribute The MaterialAttribute to remove.
	 * @return the value that was removed from this container, or null if the value was previously unset
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeMaterialAttribute(MaterialAttribute<T> attribute) {
		return (T) attributes.remove(attribute);
	}
	
	@Override
	public void clearMaterialAttributes() {
		attributes.clear();
	}
	
	public Collection<MaterialAttribute<?>> attributes() {
		return attributes.keySet();
	}
	
	public SimpleMaterialAttributeContainer copy() {
		SimpleMaterialAttributeContainer result = new SimpleMaterialAttributeContainer();
		for(Map.Entry<MaterialAttribute<?>, Object> entry : this.attributes.entrySet()) {
			result.attributes.put(entry.getKey(), entry.getValue());
		}
		
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof SimpleMaterialAttributeContainer)) return false;
		SimpleMaterialAttributeContainer that = (SimpleMaterialAttributeContainer)o;
		return (that.attributes.equals(this.attributes));
	}
	
	@Override
	public int hashCode() {
		return attributes.hashCode();
	}
}
