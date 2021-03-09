package com.playsawdust.chipper.glow.image;

import com.playsawdust.chipper.glow.util.RectangleI;

/**
 * Lays out rectangles for stitching into an AtlasImage. Note that AtlasImage does not support packing rotated sprites,
 * so some packing strategies are prohibited.
 * 
 * <p>Stitchers MAY NOT cache tiles and rearrange them.
 */
public interface Stitcher {
	/**
	 * @param image A rectangle which WILL be modified by the method to place it within the layout area. The rectangle MAY NOT be cached and moved, and the image may be pasted in immediately.
	 * @return true if the operation was successful, false if the Stitcher was unable to find sufficient layout space for the new Tile.
	 */
	public boolean stitch(RectangleI rect);
	
	/*
	
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
		int layoutSize = Math.max(256, MathUtil.nextPowerOf2(largestSize));
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
	}*/
}
