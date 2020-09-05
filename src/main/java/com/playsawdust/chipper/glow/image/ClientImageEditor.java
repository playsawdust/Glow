package com.playsawdust.chipper.glow.image;

public class ClientImageEditor implements ImageEditor {
	private ClientImage dest;
	
	public ClientImageEditor(ClientImage im) { this.dest = im; }

	@Override
	public ClientImage getImage() {
		return dest;
	}
}
