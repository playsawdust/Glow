/*
 * Glow - GL Object Wrapper
 * Copyright (C) 2020 the Chipper developers
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.playsawdust.chipper.glow.text.truetype.table;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.checkerframework.checker.nullness.qual.Nullable;

public class TTFPlatform {
	/** Enumerated constants for numeric platformId fields */
	public static enum Id {
		UNICODE(0, "Unicode"),
		MAC_OS(1, "MacOS"),
		/** This value was supposed to be used for ISO/IEC 10646, but that encoding and Unicode have identical character assignments, so this value MUST NOT be used. */
		@Deprecated
		ISO_EIC_10646(2, "ISO/EIC 10646 (deprecated)"),
		MICROSOFT(3, "Microsoft Windows"),
		UNKNOWN(-1, "Unknown");
		
		private int value;
		private String name;
		
		Id(int value, String name) {
			this.value = value;
			this.name = name;
		}
		
		public int value() { return this.value; }
		
		public static Id of(int value) {
			for(Id id : values()) if (id.value==value) return id;
			return Id.UNKNOWN;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
	}
	
	/** Represents a platformEncodingId or platformSpecificId within the UNICODE platformId */
	public static enum Unicode {
		DEFAULT(0),
		VERSION_1_1(1),
		/** This value was supposed to be used for ISO/IEC 10646, but that encoding and Unicode have identical character assignments, so this value MUST NOT be used. */
		@Deprecated
	 	ISO_10646_1993(2),
	 	VERSION_2_BMP_ONLY(3),
	 	VERSION_2_NON_BMP_ALLOWED(4),
	 	VARIATION_SEQUENCES(5),
	 	FULL_COVERAGE_OPENTYPE(6),
	 	UNKNOWN(-1);
		
		private final int value;
		
		Unicode(int value) {
			this.value = value;
		}
		
		public int value() { return this.value; }
		
		public static Unicode of(int value) {
			for(Unicode id : values()) if (id.value==value) return id;
			return Unicode.UNKNOWN;
		}
	}
	
	/** Represents a platformEncodingId or platformSpecificId within the MICROSOFT platformId */
	public static enum Microsoft {
		/* "All naming table strings for the Windows platform (platform ID 3) must be encoded in UTF-16BE."
		 * -- Microsoft OTF spec, https://docs.microsoft.com/en-us/typography/opentype/spec/name
		 */
		SYMBOL(0, StandardCharsets.UTF_16BE),
		UNICODE_BMP_ONLY_UCS2(1, StandardCharsets.UTF_16BE),
		SHIFT_JIS(2, StandardCharsets.UTF_16BE),
		PRC(3, StandardCharsets.UTF_16BE),
		BIG_FIVE(4, StandardCharsets.UTF_16BE),
		JOHAB(5, StandardCharsets.UTF_16BE),
		UNICODE_UCS4(10, StandardCharsets.UTF_16BE),
		UNKNOWN(-1);
		
		private final int value;
		private final Charset charset;
		
		Microsoft(int value) {
			this.value = value;
			this.charset = null;
		}
		
		Microsoft(int value, Charset charset) {
			this.value = value;
			this.charset = charset;
		}
		
		public int value() { return this.value; }
		public Charset charset() { return this.charset; }
		
		public static Microsoft of(int value) {
			for(Microsoft id : values()) if (id.value==value) return id;
			return Microsoft.UNKNOWN;
		}
	}
	
	public static String getPlatformString(int platformId, int platformSpecificId) {
		Id platform = Id.of(platformId);
		switch(platform) {
			case UNICODE:
				Unicode unicodeId = Unicode.of(platformSpecificId);
				return platform.toString()+" : "+unicodeId.toString();
			case MICROSOFT:
				Microsoft microsoftId = Microsoft.of(platformSpecificId);
				return platform.toString()+" : "+microsoftId.toString();
			default:
				return platform.toString()+" : "+platformSpecificId;
		}
	}
	
	public static @Nullable Charset getPlatformCharset(int platformId, int platformSpecificId) {
		Id platform = Id.of(platformId);
		if (platform==Id.MICROSOFT) {
			return StandardCharsets.UTF_16BE;
		}
		return null;
	}
}
