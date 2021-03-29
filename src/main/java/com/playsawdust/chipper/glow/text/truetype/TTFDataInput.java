/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.text.truetype;

import com.playsawdust.chipper.glow.util.RandomAccessDataInput;

public class TTFDataInput extends RandomAccessDataInput {

	public TTFDataInput(byte[] data) {
		super(data);
	}
	
	private static final double FIXED_CONVERSION_FACTOR = Math.pow(2.0, 16.0);
	public double readFixed() {
		long bits = this.readUInt32();
		long whole = bits >> 16;
		double fractional = (bits & 0xFFFF) / FIXED_CONVERSION_FACTOR; //TODO: See if we can chop off the extra fake "precision" here
		return whole + fractional;
	}
	
	public int readFWord() {
		return this.readInt16();
	}
	
	public int readUFWord() {
		return this.readUInt16();
	}
	
	public long readLongDateTime() {
		long timestamp = readInt64() * 1_000; //Seconds to millis
		return timestamp;
	}
}
