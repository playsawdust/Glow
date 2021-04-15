/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.model.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import com.playsawdust.chipper.glow.model.Model;

public interface ModelLoader {
	/**
	 * Verifies that this data matches the format of this Loader and load it.
	 * <ul>
	 *   <li>If the file cannot be verified as a match for this Loader, null is returned. (e.g. the Loader loads wavefront .obj files, but the file starts with unprintable characters)
	 *   <li>If the file type is detected to match this Loader but is malformed, an IOException is thrown.
	 *   <li>If the file type matches and the data is loaded successfully, a Model is returned representing the file contents.
	 * </ul>
	 * 
	 * <p>ModelLoaders MUST be threadsafe.
	 * @param in an InputStream containing the data to be read.
	 * @param progressConsumer an object which will receive progress reports, in whole percents. 0 means the operation is starting, 100 means the operation is about to complete.
	 * @return the Model represented by this InputStream
	 */
	public Model tryModelLoad(InputStream in, Consumer<Integer> progressConsumer) throws IOException;
	
	public default Model tryModelLoad(InputStream in) throws IOException {
		return tryModelLoad(in, (it)->{});
	}
}
