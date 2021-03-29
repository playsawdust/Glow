/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.gl;

import java.util.function.BiFunction;

import javax.annotation.ParametersAreNonnullByDefault;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.playsawdust.chipper.glow.model.MaterialAttribute;

@FunctionalInterface
@ParametersAreNonnullByDefault
public interface TextureManager extends BiFunction<String, MaterialAttribute<?>, Texture> {
	/**
	 * Called by a RenderScheduler to retrieve a Texture for binding in the near future. Any Texture
	 * returned by this method MUST be complete (in the FBO sense), and ready/available for immediate
	 * use in a draw call.
	 * @param id a unique id for the texture being requested
	 * @param attribute the MaterialAttribute the texture will be used for. If your system assigns
	 *        one ID for all textures in a material, this argument can be used to distinguish them.
	 *        Generally, texture id's are globally unique, and this argument is ignored.
	 * @return a Texture matching this id, or if no such texture exists, a "missing texture" texture.
	 */
	@Override
	public @NonNull Texture apply(String id, MaterialAttribute<?> attribute);
}
