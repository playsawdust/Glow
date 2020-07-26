package com.playsawdust.chipper.glow.model;

public interface MaterialAttributeDelegateHolder extends MaterialAttributeContainer {
	/** For internal and testing use only */
	MaterialAttributeContainer getDelegate();
	
	@Override
	default <T> T getMaterialAttribute(MaterialAttribute<T> attribute) {
		return getDelegate().getMaterialAttribute(attribute);
	}
	
	@Override
	default <T> void putMaterialAttribute(MaterialAttribute<T> attribute, T value) {
		getDelegate().putMaterialAttribute(attribute, value);
	}
	
	@Override
	default <T> T removeMaterialAttribute(MaterialAttribute<T> attribute) {
		return getDelegate().removeMaterialAttribute(attribute);
	}
	
	@Override
	default void clearMaterialAttributes() {
		getDelegate().clearMaterialAttributes();
	}
}
