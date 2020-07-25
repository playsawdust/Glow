package com.playsawdust.chipper.glow.model;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Vector3d;

public interface Material {
	/**
	 * Gets an attribute of this Material
	 * @param <T> The type of the data represented by this MaterialAttribute
	 * @param attribute the MaterialAttribute to return a value for
	 * @return the value of this MaterialAttribute, or null if no value is defined for this Material.
	 */
	public <T,U> @Nullable T getMaterialAttribute(MaterialAttribute<T> attribute);
	
	public static class BlinnPhong implements Material {
		private double specularity = 0.0;
		private Vector3d diffuseColor = new Vector3d(1, 1, 1);
		private double opacity = 1.0;
		
		private String diffuseTextureId;
		private String normalTextureId;
		private String specTextureId;
		
		@SuppressWarnings("unchecked")
		@Override
		public <T, U> @Nullable T getMaterialAttribute(MaterialAttribute<T> attribute) {
			if (attribute==MaterialAttribute.DIFFUSE_COLOR) return (T) diffuseColor;
			if (attribute==MaterialAttribute.OPACITY) return (T) Double.valueOf(opacity);
			if (attribute==MaterialAttribute.SPECULARITY) return (T) Double.valueOf(specularity);
			if (attribute==MaterialAttribute.DIFFUSE_TEXTURE_ID) return (@Nullable T) diffuseTextureId;
			if (attribute==MaterialAttribute.SPECULAR_TEXTURE_ID) return (@Nullable T) specTextureId;
			if (attribute==MaterialAttribute.NORMAL_TEXTURE_ID) return (@Nullable T) normalTextureId;
			return null;
		}
		
		public static Builder builder() {
			return new Builder();
		}
		
		public static class Builder {
			private BlinnPhong result = new BlinnPhong();
			
			public Builder diffuseColor(int rgb) {
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >>  8) & 0xFF;
				int b = (rgb      ) & 0xFF;
				
				result.diffuseColor.set(r/255.0, g/255.0, b/255.0);
				
				return this;
			}
			
			public Builder diffuseColor(double r, double g, double b) {
				result.diffuseColor.set(r, g, b);
				return this;
			}
			
			public Builder specularity(double spec) {
				result.specularity = spec;
				return this;
			}
			
			public Builder opacity(double opacity) {
				result.opacity = opacity;
				return this;
			}
			
			public Builder diffuseMap(String diffuseMap) {
				result.diffuseTextureId = diffuseMap;
				return this;
			}
			
			public Builder specularMap(String specMap) {
				result.specTextureId = specMap;
				return this;
			}
			
			public Builder normalMap(String normalMap) {
				result.normalTextureId = normalMap;
				return this;
			}
			
			public BlinnPhong build() {
				BlinnPhong r = result;
				result = null; //Prevent the Builder from further edits on this material
				return r;
			}
		}
	}
	
	/*
	public static class Generic implements Material {
		private Map<MaterialAttribute<?>, Object> attributes = new HashMap<>();
		
		//TODO: Move to builder syntax
		public <T> void set(MaterialAttribute<T> attribute, T t) {
			attributes.put(attribute, t);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> @Nullable T getMaterialAttribute(MaterialAttribute<T> attribute) {
			return (T) attributes.get(attribute);
		}
	}*/
	
}
