package com.playsawdust.chipper.glow.image;

/**
 * Lays out rectangles for stitching into an AtlasImage. Note that AtlasImage does not support packing rotated sprites,
 * so some packing strategies are prohibited.
 */
public interface Stitcher {
	public AtlasImage.Tile stitch(ClientImage image);
}
