package com.playsawdust.chipper.glow.image;

import java.util.ArrayList;
import java.util.Iterator;

import com.playsawdust.chipper.glow.util.RectangleI;

public class AtlasImage implements Iterable<RectangleI> {
	private ImageData image;
	private ArrayList<RectangleI> tiles = new ArrayList<>();
	
	private AtlasImage() {}
	
	public AtlasImage(ImageData image) {
		this.image = image;
	}
	
	public void addIsland(int tileWidth, int tileHeight) {
		addIsland(0, 0, tileWidth, tileHeight, 0, 0, -1, -1);
	}
	
	public void addIsland(int tileWidth, int tileHeight, int xpad, int ypad) {
		addIsland(0, 0, tileWidth, tileHeight, xpad, ypad, -1, -1);
	}
	
	public void addIsland(int xofs, int yofs, int tileWidth, int tileHeight, int xpad, int ypad) {
		addIsland(xofs, yofs, tileWidth, tileHeight, xpad, ypad, -1, -1);
	}
	
	public void addIsland(int xofs, int yofs, int tileWidth, int tileHeight, int xpad, int ypad, int tilesWide, int tilesHigh) {
		if (tilesWide==-1) tilesWide = (image.getWidth()-xofs+xpad) / (tileWidth+xpad);
		if (tilesHigh==-1) tilesHigh = (image.getHeight()-yofs+ypad) / (tileHeight+ypad);
		
		for(int yi=0; yi<tilesHigh; yi++) {
			int y = yofs + (tileHeight+ypad) * yi;
			for(int xi=0; xi<tilesWide; xi++) {
				int x = xofs + (tileWidth+xpad) * xi;
				
				tiles.add(new RectangleI(x, y, tileWidth, tileHeight));
			}
		}
	}
	
	public void addTile(RectangleI tile) {
		tiles.add(tile);
	}
	
	public AtlasImage of(ImageData image, int xofs, int yofs, int tileWidth, int tileHeight, int xpad, int ypad) {
		return of(image, xofs, yofs, tileWidth, tileHeight, xpad, ypad, -1, -1);
	}
	
	public AtlasImage of(ImageData image, int xofs, int yofs, int tileWidth, int tileHeight, int xpad, int ypad, int tilesWide, int tilesHigh) {
		AtlasImage result = new AtlasImage();
		result.image = image;
		result.addIsland(xofs, yofs, tileWidth, tileHeight, xpad, ypad, tilesWide, tilesHigh);
		return result;
	}
	
	public RectangleI getTile(int index) {
		return tiles.get(index);
	}
	
	public ImageData getImage() {
		return image;
	}
	
	public ImageData getAsImage(int index) {
		RectangleI t = getTile(index);
		ImageData result = new ImageData(t.width(), t.height());
		for(int y=0; y<t.width(); y++) {
			for(int x=0; x<t.height(); x++) {
				result.setPixel(x, y, image.getPixel(x+t.x(), y+t.y()));
			}
		}
		return result;
	}
	
	public Iterator<RectangleI> iterator() {
		return tiles.iterator();
	}
}
