package com.playsawdust.chipper.glow.util;

import com.playsawdust.chipper.glow.gl.GPUResource;

/** Objects implementing this class maintain BOTH native / offheap memory AND on-GPU objects. Callers MUST call {@link #free()} before allowing this object to finalize.
 * 
 * <p>As with each individual class of managed resource, this class extends {@link AutoCloseable}, which means that implementations may be used as resources in try-with-resources statements.</p>
 */
public abstract class AbstractCombinedResource extends AbstractOffheapResource implements GPUResource {

}
