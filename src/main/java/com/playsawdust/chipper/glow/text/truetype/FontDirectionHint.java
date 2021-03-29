/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.text.truetype;

public enum FontDirectionHint {
	MIXED(0), LTR(1), LTR_AND_NEUTRALS(2), RTL(-1), RTL_AND_NEUTRALS(-2);
	
	private int value;
	
	FontDirectionHint(int value) {
		this.value = value;
	}
	
	public int value() { return value; }
	
	public static FontDirectionHint of(int value) {
		for(FontDirectionHint hint : values()) {
			if (hint.value()==value) return hint;
		}
		return FontDirectionHint.MIXED;
	}
}
