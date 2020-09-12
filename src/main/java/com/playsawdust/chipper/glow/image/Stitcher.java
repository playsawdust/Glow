package com.playsawdust.chipper.glow.image;

import java.util.ArrayList;
import java.util.Collection;

import com.playsawdust.chipper.glow.util.RectangleI;

/**
 * Lays out rectangles for stitching into an AtlasImage. Note that AtlasImage does not support packing rotated sprites,
 * so some packing strategies are prohibited.
 * 
 * <p>Stitchers MAY cache previously-emitted Tiles and modify them, and MAY inspect existing Tiles on the destination
 * AtlasImage in order to discover gaps or areas which may be restitched. No notification is given to a Stitcher when
 * there are no more images to stitch, so when each call to stitch returns, the Tiles MUST be in a valid state to end
 * the stitching process and write all the ClientImages into the destination AtlasImage.
 */
public interface Stitcher {
	/**
	 * @param area The total allowable area for layout. This method MUST be called with the same Tile every single time for the entire layout process.
	 * @param image A rectangle which WILL be modified by the method to place it within the layout area. This rectangle MAY be cached and moved again to accomodate subsequent requests.
	 * @return true if the operation was successful, false if the Stitcher was unable to find sufficient layout space for the new Tile.
	 */
	public boolean stitch(AtlasImage.Tile area, AtlasImage.Tile image);
	
	public static AtlasImage createAtlas(Stitcher stitcher, Collection<ClientImage> sprites) {
		RectangleI initialArea = new RectangleI(0, 0, 256, 256);
		ArrayList<LayoutArea> layoutAreas = new ArrayList<>();
		
		
		
		return null; //TODO: what if we need to resize the image?!? We really need to layout "within a region"
	}
	
	public static class LayoutArea  {
		RectangleI area;
	}
}
