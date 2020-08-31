/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
