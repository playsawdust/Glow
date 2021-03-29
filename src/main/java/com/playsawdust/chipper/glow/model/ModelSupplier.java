/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.model;

import java.util.Iterator;

/**
 * Represents a Modifier (like Blender) or a dynamically-assembled model.
 */
public interface ModelSupplier {
	public Iterator<MeshSupplier> supplyMeshes();
}
