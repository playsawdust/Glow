/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.image;

public class EmergencyBitmapFont {
	private static final int[] FONT = {
			0x0802108, 0x0000294, 0x15f57d4, 0x08c330c, 0x22820a2, 0x0ab2288, 0x0000108, 0x0884208, 0x0821088, 0x0005114,
			0x084f908, 0x1040000, 0x000f800, 0x0800000, 0x1042104, 0x1d1ac5c, 0x1c42308, 0x3e8345c, 0x3c1307c, 0x042fca4,
			0x3c1f43e, 0x1d1f41e, 0x104107e, 0x1d1745c, 0x1c17c5c, 0x0040100, 0x1040100, 0x06c8306, 0x01f07c0, 0x30609b0,
			0x080345c, 0x3d5485c, 0x231fc5c, 0x3d1f47c, 0x1f0841e, 0x3d18c7c, 0x3f0c43e, 0x210c43e, 0x1f19c1e, 0x231fc62,
			0x3e4213e, 0x3c1087e, 0x232e4a2, 0x3f08420, 0x231aee2, 0x233ae62, 0x1d18c5c, 0x210f47c, 0x1b2ac5c, 0x231f47c,
			0x3c1741e, 0x084213e, 0x1d18c62, 0x08a5462, 0x23bac62, 0x2317462, 0x0847462, 0x3e820be, 0x1c8421c, 0x0442110,
			0x1c2109c, 0x0000288, 0x3e00000, 0x0000110, 0x1b38b80, 0x3d18fa0, 0x1f083c0, 0x1f18bc2, 0x1d0fc5c, 0x108e24c,
			0x1c17c5c, 0x2318fa0, 0x0842008, 0x1d10802, 0x232a62c, 0x1f08420, 0x2b5ae80, 0x2318f80, 0x1d18b80, 0x21e8b80,
			0x06e9380, 0x2108f80, 0x182220c, 0x08427c8, 0x1f18c40, 0x08a5440, 0x1558c40, 0x2317440, 0x1c17c40, 0x3ec37c0,
			0x0c8c20c, 0x0842108, 0x1821898, 0x0007000 };



	
	public static void paint(ImageData im, int x, int y, char ch, int color) {
		if (ch<0x21) return; //Unprintables plus space
		int index = ch - 0x21;
		if (index>FONT.length) return;
		
		int image = FONT[index];
		for(int iy=0; iy<5; iy++) {
			for(int ix=0; ix<5; ix++) {
				int bitIndex = iy*5+ (5-ix);
				int bit = (image >> bitIndex) & 0x01;
				
				if (bit==1) im.setPixel(x+ix, y+iy, color);
			}
		}
	}
	
	public static void paint(ImageData im, int x, int y, CharSequence s, int color) {
		for(int i=0; i<s.length(); i++) {
			paint(im, x+(i*6), y, s.charAt(i), color);
		}
	}
}
