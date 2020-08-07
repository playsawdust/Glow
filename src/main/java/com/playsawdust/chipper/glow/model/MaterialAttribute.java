package com.playsawdust.chipper.glow.model;

import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Represents some quality of a surface or a property at a vertex. For instance, UV location, diffuse color, or specularity.
 * Attributes could exist at either the vertex or material granularity. If a vertex value exists, and the pass supports per-vertex
 * values, vertices will override the material.
 */
public class MaterialAttribute<T> {
	private String name;
	private Class<T> clazz;
	private T defaultValue;
	
	public MaterialAttribute(String name, Class<T> clazz, T defaultValue) {
		this.name = name;
	}
	
	public String getName() { return name; }
	
	public Class<T> getDataClass() { return clazz; }
	
	public T getDefaultValue() {
		return defaultValue;
	}
	
	public String toString() {
		return "{ \"MaterialAttribute\": \""+name+"\" }";
	}
	
	/* Texture map IDs for Blinn-Phong
	 * Intended for Material use. Texture IDs shouldn't be used in vertex attributes unless you're submitting to a texarray pass or flattening them away with an atlas model.
	 */
	public static MaterialAttribute<String> DIFFUSE_TEXTURE_ID = new MaterialAttribute<>("diffuseTextureId", String.class, "untitled");
	public static MaterialAttribute<String> SPECULAR_TEXTURE_ID = new MaterialAttribute<>("specularTextureId", String.class, "untitled");
	public static MaterialAttribute<String> NORMAL_TEXTURE_ID = new MaterialAttribute<>("normalTextureId", String.class, "untitled");
	
	
	/*
	 * Blinn-Phong attributes
	 * This is a simple lighting model which basically any renderer/pass/shader will support.
	 */
	
	public static MaterialAttribute<Vector3dc> DIFFUSE_COLOR = new MaterialAttribute<>("diffuseColor", Vector3dc.class, new Vector3d(0,0,0));
	public static MaterialAttribute<Double> SPECULARITY = new MaterialAttribute<>("specularity", Double.class, 0.0);
	
	/** The surface normal in model-space.*/
	public static MaterialAttribute<Vector3dc> NORMAL = new MaterialAttribute<>("normal", Vector3dc.class, new Vector3d(0,0,0));
	
	/**
	 * A model-space vector 90 degrees away from the normal vector, pointing so that it will skim across ("tangent to") the surface left-to-right. Between the normal and
	 * the tangent, a matrix can be constructed to translate back and forth between model-space (where {0,0,1} points towards the front of the model) and tangent-space
	 * (where {0,0,1} points "up" out of the polygon). Since the incoming light and the camera vector are available in model-space or world-space, this enables normal maps
	 * to be constructed in their "own" local space but used at different angles outside the XZ plane.
	 */
	public static MaterialAttribute<Vector3dc> TANGENT = new MaterialAttribute<>("tangent", Vector3dc.class, new Vector3d(0,0,0));
	
	/** This can be found from the crossproduct of the normal and tangent inside the vertex shader, so generally this shouldn't be used for material or vertex attributes. */
	public static MaterialAttribute<Vector3dc> BITANGENT = new MaterialAttribute<>("bitangent", Vector3dc.class, new Vector3d(0,0,0));
	
	/** At 0 opacity, geometry is invisible. At 1 opacity, geometry is completely solid. Note that opacity can be controled at a finer-grained level using a diffuse texture. */
	public static MaterialAttribute<Double> OPACITY = new MaterialAttribute<>("opacity", Double.class, 0.0);
	
	/*
	 * PBR workflow attributes
	 * These attributes aren't required, and often won't be provided. Authoring PBR materials can be very difficult, and not all renderers/passes/shaders support them.
	 * But in the right hands, PBR materials give a surface an ineffable feeling of realism that simply can't be achieved in Blinn-Phong models.
	 */
	
	/**
	 * Albedo is distinct from diffuse color by having all shadows, AO, and surface detail subtracted out. As a result, albedo colors often look
	 * really bright and washed-out. This can be hard to create assets for. Unless you're really intent on using a PBR workflow, use {@link #DIFFUSE_COLOR} instead.
	 */
	public static MaterialAttribute<Double> ALBEDO = new MaterialAttribute<>("albedo", Double.class, 0.0);
	
	/**
	 * Low metalness represents a dielectric material with a diffuse color. High metalness represents a conductive material with high reflectivity,
	 * but *no* diffuse color due to conservation of energy. In reality, optically, there are only full-dielectrics and full-conductors with different roughness and
	 * index of refraction. If you just want a "make it shinier" knob that you don't have to think about, use {@link #SPECULARITY} instead.
	 */
	public static MaterialAttribute<Double> METALNESS = new MaterialAttribute<>("metalness", Double.class, 0.0);
	
	/**
	 * Low smoothness scatters specular light, making reflections indistinct and causing diffuse interactions to be more pronounced. High smoothness creates a crisp mirror
	 * finish where specular interactions dominate.
	 */
	public static MaterialAttribute<Double> SMOOTHNESS = new MaterialAttribute<>("smoothness", Double.class, 0.0);
	
	/**
	 * Index of Refraction controls the light which passes through the material's surface, including diffuse light which gets bounced back out after a negligible distance.
	 * How much light gets bent depends on both the IOR of the material the light is *exiting* and the IOR it's *entering*. If the IORs are the same, the light is not
	 * bent. This is a useless slider for metals, where refracted light is absorbed.
	 */
	public static MaterialAttribute<Double> INDEX_OF_REFRACTION = new MaterialAttribute<>("indexOfRefraction", Double.class, 0.0);
	
	public static MaterialAttribute<String> ALBEDO_TEXTURE_ID = new MaterialAttribute<>("albedoTextureId", String.class, "untitled");
	public static MaterialAttribute<String> METALNESS_TEXTURE_ID = new MaterialAttribute<>("metalnessTextureId", String.class, "untitled");
	public static MaterialAttribute<String> SMOOTHNESS_TEXTURE_ID = new MaterialAttribute<>("smoothnessTextureId", String.class, "untitled");
}
