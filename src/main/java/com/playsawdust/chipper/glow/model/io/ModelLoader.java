package com.playsawdust.chipper.glow.model.io;

import java.io.IOException;
import java.io.InputStream;

import com.playsawdust.chipper.glow.model.Model;

public interface ModelLoader {
	/**
	 * Verifies that this data matches the format of this Loader and load it.
	 * <ul>
	 *   <li>If the file cannot be verified as a match for this Loader, null is returned. (e.g. the Loader loads wavefront .obj files, but the file starts with unprintable characters)
	 *   <li>If the file type is detected to match this Loader but is malformed, an IOException is thrown.
	 *   <li>If the file type matches and the data is loaded successfully, a Model is returned representing the file contents.
	 * </ul>
	 * @param in an InputStream containing the data to be read.
	 * @return
	 */
	public Model tryLoad(InputStream in) throws IOException;
}
