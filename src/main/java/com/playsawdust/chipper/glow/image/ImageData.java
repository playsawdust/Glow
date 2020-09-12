package com.playsawdust.chipper.glow.image;

public class ImageData {
	private int width = 0;
	private int height = 0;
	private int[] data = new int[0];
	
	public ImageData() {}
	
	public ImageData(int width, int height) {
		this.width = width;
		this.height = height;
		this.data = new int[width*height];
	}
	
	public ImageData(int width, int height, int[] data) {
		this.width = width;
		this.height = height;
		this.data = data;
	}
	
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
	public int[] getData() { return this.data; }
	
	public void setPixel(int x, int y, int argb) {
		if (x<0 || x>=width || y<0 || y>=width) return;
		data[y*width + x] = argb;
	}
	
	public int getPixel(int x, int y) {
		if (x<0 || x>=width || y<0 || y>=width) return 0;
		return data[y*width + x];
	}
}
