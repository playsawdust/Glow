/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.gl.shader;

public class ShaderError extends Exception {
	private static final long serialVersionUID = 1308943047989778277L;
	private String infoLog = "";
	
	public ShaderError() {
		super();
	}
	
	public ShaderError(String message) {
		super(message);
	}
	
	public ShaderError(String message, Exception source) {
		super(message, source);
	}
	
	public ShaderError(String message, String infoLog) {
		super(message);
		this.infoLog = infoLog;
	}
	
	public boolean hasInfoLog() {
		return infoLog.isBlank();
	}
	
	public String getInfoLog() {
		return infoLog;
	}
}
