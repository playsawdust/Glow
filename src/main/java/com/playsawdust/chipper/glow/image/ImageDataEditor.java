package com.playsawdust.chipper.glow.image;

public class ImageDataEditor implements ImageEditor {
	private ImageData dest;
	
	public ImageDataEditor(ImageData im) { this.dest = im; }

	@Override
	public ImageData getImage() {
		return dest;
	}
}
