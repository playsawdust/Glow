/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.model;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface MaterialAttributeContainer {
	/**
	 * Gets a MaterialAttribute setting from this container.
	 * @param <T> The value type for this attribute
	 * @param attribute The MaterialAttribute key to get a value for
	 * @return The value mapped to the provided attribute, or null if no value is mapped.
	 */
	<T> @Nullable T getMaterialAttribute(@NonNull MaterialAttribute<T> attribute);
	
	/**
	 * Adds a MaterialAttribute setting to this container.
	 * @param <T> The value type for this attribute
	 * @param attribute The MaterialAttribute key to set a value for
	 * @param t The value to map this MaterialAttribute to for this container
	 */
	<T> void putMaterialAttribute(@NonNull MaterialAttribute<T> attribute, @NonNull T value);
	
	/**
	 * Removes a MaterialAttribute setting from this container. When this call is complete, whether or not there
	 * was originally a setting for this attribute, getMaterialAttribute will return null for this attribute.
	 * @param attribute The MaterialAttribute to remove.
	 * @return the value that was removed from this container, or null if the value was previously unset
	 */
	<T> @Nullable T removeMaterialAttribute(@NonNull MaterialAttribute<T> attribute);
	
	/**
	 * Clears all MaterialAttributes from this container. No attributes will be set after calling this method.
	 */
	void clearMaterialAttributes();
}
