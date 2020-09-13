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
