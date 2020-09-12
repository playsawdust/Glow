package com.playsawdust.chipper.glow.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Supplier;

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
	 * @param area The total allowable area for layout. This method MUST be called with the same Rectangle every single time for the entire layout process.
	 * @param image A rectangle which WILL be modified by the method to place it within the layout area. This rectangle MAY be cached and moved again to accomodate subsequent requests.
	 * @return true if the operation was successful, false if the Stitcher was unable to find sufficient layout space for the new Tile.
	 */
	public boolean stitch(RectangleI area, RectangleI image);
	
	
	
	
	
	public static AtlasImage createAtlas(Supplier<Stitcher> stitcherFactory, List<ImageData> sprites, int limit, HashMap<ImageData, Integer> indexMap) {
		TreeSet<ImageData> sorted = new TreeSet<>(
			(a, b)-> Integer.compare(
					 Math.max(a.getWidth(), a.getHeight()),
					 Math.max(b.getWidth(), b.getHeight())
					)
		);
		
		for(ImageData sprite : sprites) sorted.add(sprite);
		ImageData largest = sorted.last();
		int largestSize = Math.max(largest.getWidth(), largest.getHeight());
		
		
		LayoutArea initialArea = new LayoutArea();
		int layoutSize = Math.max(256, nextPowerOf2(largestSize));
		if (layoutSize>limit) {
			throw new IllegalArgumentException("Cannot specify a size limit smaller than the images to layout"); //TODO: Is there a better way to handle this?
		}
		initialArea.area = new RectangleI(0, 0, layoutSize, layoutSize);
		initialArea.stitcher = stitcherFactory.get();
		ArrayList<LayoutArea> layoutAreas = new ArrayList<>();
		layoutAreas.add(initialArea);
		
		
		while (!sorted.isEmpty()) {
			ImageData im = sorted.last();
			RectangleI cur = new RectangleI(0, 0, im.getWidth(), im.getHeight());
			boolean hasLayout = false;
			for(LayoutArea area : layoutAreas) {
				if (area.stitch(cur, im)) {
					hasLayout = true;
					break;
				}
			}
			
			if (!hasLayout) {
				if (limit>0 && layoutSize>limit) {
					break; //Bail and lay out everything we can.
				}
				
				LayoutArea bottomArea = new LayoutArea(); bottomArea.stitcher = stitcherFactory.get();
				LayoutArea rightArea = new LayoutArea();  rightArea.stitcher = stitcherFactory.get();
				
				bottomArea.area = new RectangleI(0, layoutSize, layoutSize, layoutSize);
				rightArea.area = new RectangleI(layoutSize, 0, layoutSize, layoutSize*2);
				layoutSize = layoutSize * 2;
				
				layoutAreas.add(bottomArea);
				layoutAreas.add(rightArea);
			} else {
				sorted.remove(im);
			}
		}
		
		ImageData atlas = new ImageData(layoutSize, layoutSize);
		AtlasImage result = new AtlasImage(atlas);
		ImageEditor editor = ImageEditor.edit(atlas);
		
		int index = 0;
		for(LayoutArea area : layoutAreas) {
			for(Map.Entry<RectangleI, ImageData> entry : area.images.entrySet()) {
				indexMap.put(entry.getValue(), index);
				result.addTile(entry.getKey());
				editor.drawImage(entry.getValue(), entry.getKey().x(), entry.getKey().y());
				index++;
			}
		}
		
		return result;
	}
	
	public static class LayoutArea  {
		Stitcher stitcher;
		RectangleI area;
		HashMap<RectangleI, ImageData> images = new HashMap<>();
		
		public boolean stitch(RectangleI rect, ImageData image) {
			if (stitcher.stitch(area, rect)) {
				images.put(rect, image);
				return true;
			} else {
				return false;
			}
		}
	}
	
	/** Lifted from geeks4geeks.org */
	private static int nextPowerOf2(int n) { 
		n--; 
		n |= n >> 1; 
		n |= n >> 2; 
		n |= n >> 4; 
		n |= n >> 8; 
		n |= n >> 16; 
		n++; 
		
		return n; 
	}
}
