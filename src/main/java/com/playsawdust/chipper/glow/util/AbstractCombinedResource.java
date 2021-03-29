/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.util;

import com.playsawdust.chipper.glow.gl.GPUResource;

/** Objects implementing this class maintain BOTH native / offheap memory AND on-GPU objects. Callers MUST call {@link #free()} before allowing this object to finalize.
 * 
 * <p>As with each individual class of managed resource, this class extends {@link AutoCloseable}, which means that implementations may be used as resources in try-with-resources statements.</p>
 */
public abstract class AbstractCombinedResource extends AbstractOffheapResource implements GPUResource {

}
