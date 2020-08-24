package com.playsawdust.chipper.glow.model;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public interface Material extends MaterialAttributeContainer {
	public static Generic GENERIC = new Generic()
			.with(MaterialAttribute.DIFFUSE_COLOR, new Vector3d(1,1,1))
			.with(MaterialAttribute.OPACITY, Double.valueOf(1.0))
			.with(MaterialAttribute.SPECULARITY, Double.valueOf(0.3))
			.with(MaterialAttribute.EMISSIVITY, 0.0)
			.with(MaterialAttribute.DIFFUSE_TEXTURE_ID, "none")
			.freeze();
	
	public static Generic RED_PLASTIC = new Generic()
			.with(MaterialAttribute.DIFFUSE_COLOR, new Vector3d(1.0, 0.5, 0.5))
			.with(MaterialAttribute.OPACITY, Double.valueOf(1.0))
			.with(MaterialAttribute.SPECULARITY, Double.valueOf(0.6))
			.with(MaterialAttribute.EMISSIVITY, 0.0)
			.with(MaterialAttribute.DIFFUSE_TEXTURE_ID, "none")
			.freeze();
	
	
	/** Immutable Material exposing Blinn-Phong attributes */
	public static class BlinnPhong implements Material {
		private double specularity = 0.0;
		private @NonNull Vector3d diffuseColor = new Vector3d(1, 1, 1);
		private double opacity = 1.0;
		
		private @Nullable String diffuseTextureId;
		private @Nullable String normalTextureId;
		private @Nullable String specTextureId;
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> @Nullable T getMaterialAttribute(MaterialAttribute<T> attribute) {
			if (attribute==MaterialAttribute.DIFFUSE_COLOR) return (T) diffuseColor;
			if (attribute==MaterialAttribute.OPACITY) return (T) Double.valueOf(opacity);
			if (attribute==MaterialAttribute.SPECULARITY) return (T) Double.valueOf(specularity);
			if (attribute==MaterialAttribute.DIFFUSE_TEXTURE_ID) return (@Nullable T) diffuseTextureId;
			if (attribute==MaterialAttribute.SPECULAR_TEXTURE_ID) return (@Nullable T) specTextureId;
			if (attribute==MaterialAttribute.NORMAL_TEXTURE_ID) return (@Nullable T) normalTextureId;
			
			return null;
		}
		
		@Override
		public <T> void putMaterialAttribute(MaterialAttribute<T> attribute, T value) {
			if (attribute==MaterialAttribute.DIFFUSE_COLOR) { diffuseColor = new Vector3d((Vector3d) value); return; }
			if (attribute==MaterialAttribute.OPACITY) { opacity = (Double)value; return; }
			if (attribute==MaterialAttribute.SPECULARITY) { specularity = (Double)value; return; }
			if (attribute==MaterialAttribute.DIFFUSE_TEXTURE_ID) { diffuseTextureId = (String)value; return; }
			if (attribute==MaterialAttribute.SPECULAR_TEXTURE_ID) { specTextureId = (String)value; return; }
			if (attribute==MaterialAttribute.NORMAL_TEXTURE_ID) { normalTextureId = (String)value; return; }
		}
		
		//TODO: Return old value
		@Override
		public <T> @Nullable T removeMaterialAttribute(MaterialAttribute<T> attribute) {
			if (attribute==MaterialAttribute.DIFFUSE_COLOR) { diffuseColor.set(1, 1, 1); return null; }
			if (attribute==MaterialAttribute.OPACITY) { opacity = 1.0; return null; }
			if (attribute==MaterialAttribute.SPECULARITY) { specularity = 0.0; return null; }
			if (attribute==MaterialAttribute.DIFFUSE_TEXTURE_ID) { diffuseTextureId = null; return null; }
			if (attribute==MaterialAttribute.SPECULAR_TEXTURE_ID) { specTextureId = null; return null; }
			if (attribute==MaterialAttribute.NORMAL_TEXTURE_ID) { normalTextureId = null; return null; }
			
			return null;
		}

		@Override
		public void clearMaterialAttributes() {
			specularity = 0.0;
			diffuseColor.set(1, 1, 1);
			opacity = 1.0;
			diffuseTextureId = null;
			normalTextureId = null;
			specTextureId = null;
		}
		
		public double getSpecularity() { return specularity; }
		public double getOpacity() { return opacity; }
		public Vector3dc getDiffuseColor() { return diffuseColor; }
		public void getDiffuseColor(Vector3d result) { result.set(diffuseColor); }
		public @Nullable String getDiffuseTextureId() { return diffuseTextureId; }
		public @Nullable String getNormalTextureId() { return normalTextureId; }
		public @Nullable String getSpecularTextureId() { return specTextureId; }
		
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
	
	/** Mutable material that starts with no attributes, suitable for any material workflow. Can be frozen to create immutable objects. */
	public static class Generic implements Material, MaterialAttributeDelegateHolder {
		private volatile boolean frozen = false;
		private SimpleMaterialAttributeContainer delegate = new SimpleMaterialAttributeContainer();

		@Override
		public MaterialAttributeContainer getDelegate() {
			return delegate;
		}
		
		public <T> Generic with(MaterialAttribute<T> attrib, T value) {
			if (frozen) throw new IllegalStateException("Cannot edit a frozen Material");
			delegate.putMaterialAttribute(attrib, value);
			return this;
		}
		
		public Generic freeze() {
			this.frozen = true;
			return this;
		}
		
		boolean isFrozen() {
			return frozen;
		}
		
		@Override
		public <T> void putMaterialAttribute(@NonNull MaterialAttribute<T> attribute, @NonNull T value) {
			if (frozen) throw new IllegalStateException("Cannot edit a frozen Material");
			delegate.putMaterialAttribute(attribute, value);
		}
		
		/** Returns an *unfrozen* copy of this Material, with idential attributes. */
		public Generic copy() {
			Generic result = new Generic();
			for(MaterialAttribute<?> attribute : delegate.attributes()) {
				copyAttribute(attribute, delegate, result);
			}
			return result;
		}
		
		/** You can't get this level of hackery inline, but this is actually statically verified */
		private static <T> void copyAttribute(MaterialAttribute<T> attribute, MaterialAttributeContainer source, MaterialAttributeContainer target) {
			T t = source.getMaterialAttribute(attribute);
			target.putMaterialAttribute(attribute, t);
		}
		
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("{\n");
			
			for(MaterialAttribute<?> attrib : delegate.attributes()) {
				result.append("\"");
				result.append(attrib.getName());
				result.append("\": ");
				result.append(delegate.getMaterialAttribute(attrib));
				result.append(",\n");
			}
			
			result.append("}");
			return result.toString();
		}
	}
}
