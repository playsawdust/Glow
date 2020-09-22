package com.playsawdust.chipper.glow.image.atlas;

import com.playsawdust.chipper.glow.image.ImageData;
import com.playsawdust.chipper.glow.util.RectangleI;

public interface Atlas {
	/** Gets the number of images in this Atlas */
	public int imageCount();
	
	/**
	 * Gets a rectangle describing the area of this Atlas covered by the indicated subimage.
	 * @param index the image to get the location and size of
	 */
	public RectangleI getImageRegion(int index);
	
	/**
	 * Creates a standalone copy of the indicated subimage
	 * @param index the subimage to copy out
	 */
	public ImageData getImage(int index);
	
	/**
	 * Copies the image into the area previously occupied by another subimage.
	 * @param index the subimage to replace
	 * @param image the new image data to put in its place
	 */
	public void replaceImage(int index, ImageData image);
}
