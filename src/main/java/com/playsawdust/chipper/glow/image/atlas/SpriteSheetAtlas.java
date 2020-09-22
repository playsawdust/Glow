package com.playsawdust.chipper.glow.image.atlas;

import com.playsawdust.chipper.glow.image.ImageData;
import com.playsawdust.chipper.glow.util.RectangleI;

/**
 * Represents an Atlas image where the source is a prearranged image, already containing all of the subimages to be
 * rendered or copied out from it. Atlases like this are frequently used for particles, character
 * sprites and sprite animations, and HUD /  GUI icons.
 */
public class SpriteSheetAtlas extends AbstractAtlas {
	
	/**
	 * Creates an Atlas from the image, but designates no islands of subimages. Complicated sprite
	 * sheet layouts may want to use this constructor, and then call {@link #addIsland(int, int, int, int, int, int, int, int)}
	 * one or more times to describe the subimage layout.
	 * 
	 * @param image the image to create this Atlas from
	 */
	public SpriteSheetAtlas(ImageData image) {
		int[] otherData = image.getData();
		this.data = new int[otherData.length];
		System.arraycopy(otherData, 0, this.data, 0, otherData.length);
		this.width = image.getWidth();
		this.height = image.getHeight();
	}
	
	/**
	 * Creates an Atlas from the image, and designates numbered subimages across the entire area,
	 * with no gaps between them.
	 * 
	 * @param image the image to create this Atlas from
	 * @param tileWidth the width of each subimage
	 * @param tileHeight the height of each subimage
	 */
	public SpriteSheetAtlas(ImageData image, int tileWidth, int tileHeight) {
		this(image);
		addIsland(0, 0, tileWidth, tileHeight, 0, 0, -1, -1);
	}
	
	/**
	 * Creates an Atlas from the image, and designates numbered subimages across the entire area,
	 * with the specified gaps between each subimage. The subimages are assumed to start on the very
	 * first x/y pixel of the image
	 * @param image the image to create this Atlas from
	 * @param tileWidth the width of each subimage
	 * @param tileHeight the height of each subimage
	 * @param xpad the number of pixels horizontally between each subimage
	 * @param ypad the number of pixels vertically between each subimage
	 */
	public SpriteSheetAtlas(ImageData image, int tileWidth, int tileHeight, int xpad, int ypad) {
		this(image);
		addIsland(0, 0, tileWidth, tileHeight, xpad, ypad, -1, -1);
	}
	
	/**
	 * Creates an Atlas from the image, and designates numbered subimages across the entire area,
	 * with the specified gaps between each subimage, and offset from the top-left corner of the
	 * image.
	 * 
	 * @param image the image to create this Atlas from
	 * @param xofs the left edge where subimages start
	 * @param yofs the top edge where subimages start
	 * @param tileWidth the width of each subimage
	 * @param tileHeight the height of each subimage
	 * @param xpad the number of pixels horizontally between each subimage
	 * @param ypad the number of pixels vertically between each subimage
	 */
	public SpriteSheetAtlas(ImageData image, int xofs, int yofs, int tileWidth, int tileHeight, int xpad, int ypad) {
		this(image);
		addIsland(xofs, yofs, tileWidth, tileHeight, xpad, ypad, -1, -1);
	}
	
	/**
	 * Designates the entire area of this SpriteSheetAtlas as an island of identically-sized images.
	 * Convenience version of {@link #addIsland(int, int, int, int, int, int, int, int)}.
	 * 
	 * @param tileWidth the width of each subimage
	 * @param tileHeight the height of each subimage
	 * 
	 * @return the number of subimages added to this SpriteSheetAtlas from this island
	 */
	public void addIsland(int tileWidth, int tileHeight) {
		addIsland(0, 0, tileWidth, tileHeight, 0, 0, -1, -1);
	}
	
	/**
	 * Designates the entire area of this SpriteSheetAtlas as an island of identically-sized images.
	 * Convenience version of {@link #addIsland(int, int, int, int, int, int, int, int)}.
	 * 
	 * @param tileWidth the width of each subimage
	 * @param tileHeight the height of each subimage
	 * @param xpad the number of pixels horizontally between each subimage
	 * @param ypad the number of pixels vertically between each subimage
	 * 
	 * @return the number of subimages added to this SpriteSheetAtlas from this island
	 */
	public int addIsland(int tileWidth, int tileHeight, int xpad, int ypad) {
		return addIsland(0, 0, tileWidth, tileHeight, xpad, ypad, -1, -1);
	}
	
	/**
	 * Designates a region of this SpriteSheetAtlas as an island of identically-sized images.
	 * Convenience version of {@link #addIsland(int, int, int, int, int, int, int, int)}.
	 * 
	 * @param xofs the left edge of the island of subimages
	 * @param yofs the top edge of the island of subimages
	 * @param tileWidth the width of each subimage
	 * @param tileHeight the height of each subimage
	 * @param xpad the number of pixels horizontally between each subimage
	 * @param ypad the number of pixels vertically between each subimage
	 * 
	 * @return the number of subimages added to this SpriteSheetAtlas from this island
	 */
	public int addIsland(int xofs, int yofs, int tileWidth, int tileHeight, int xpad, int ypad) {
		return addIsland(xofs, yofs, tileWidth, tileHeight, xpad, ypad, -1, -1);
	}
	
	/**
	 * Designates a region of this SpriteSheetAtlas as an island of identically-sized images. Returns the number of
	 * images added.
	 * 
	 * <p>Each new island adds subimages to the *end* of the list, so their indices continue to grow upwards. In other
	 * words, if you add a 2x2 island to an empty SpriteSheetAtlas, they will occupy indices [0..3] inclusive. If you
	 * then add a 4x2 island, the new island will occupy indices [4..11] inclusive.
	 * 
	 * @param xofs the left edge of the island of subimages
	 * @param yofs the top edge of the island of subimages
	 * @param tileWidth the width of each subimage
	 * @param tileHeight the height of each subimage
	 * @param xpad the number of pixels horizontally between each subimage
	 * @param ypad the number of pixels vertically between each subimage
	 * @param tilesWide the number of subimages per row in this island, or -1 to calculate from available image space
	 * @param tilesHigh the number of rows of subimages in this island, or -1 to calculate from available image space
	 * 
	 * @return the number of subimages added to this SpriteSheetAtlas from this island
	 */
	public int addIsland(int xofs, int yofs, int tileWidth, int tileHeight, int xpad, int ypad, int tilesWide, int tilesHigh) {
		if (tilesWide==-1) tilesWide = (width-xofs+xpad) / (tileWidth+xpad);
		if (tilesHigh==-1) tilesHigh = (height-yofs+ypad) / (tileHeight+ypad);
		
		int imageCount = 0;
		for(int yi=0; yi<tilesHigh; yi++) {
			int y = yofs + (tileHeight+ypad) * yi;
			for(int xi=0; xi<tilesWide; xi++) {
				int x = xofs + (tileWidth+xpad) * xi;
				
				images.add(new RectangleI(x, y, tileWidth, tileHeight));
				imageCount++;
			}
		}
		
		return imageCount;
	}
}
