/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow;

/**
 * Thrown when an attempt is made to use a semi-managed, native, or GPU object after it
 * has been freed.
 */
public class UseAfterFreeException extends RuntimeException {
	private static final long serialVersionUID = 337624480506027119L;

	public UseAfterFreeException() {
		super();
	}

	public UseAfterFreeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UseAfterFreeException(String message) {
		super(message);
	}

	public UseAfterFreeException(Throwable cause) {
		super(cause);
	}
}
