package com.playsawdust.chipper.glow.image;

import java.util.ArrayList;
import java.util.Iterator;

public class AtlasImage implements Iterable<AtlasImage.Tile> {
	private ClientImage image;
	private ArrayList<Tile> tiles = new ArrayList<>();
	
	private AtlasImage() {}
	
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
				
				tiles.add(new Tile(x, y, tileWidth, tileHeight));
			}
		}
	}
	
	public void addTile(Tile tile) {
		tiles.add(tile);
	}
	
	public AtlasImage of(ClientImage image, int xofs, int yofs, int tileWidth, int tileHeight, int xpad, int ypad) {
		return of(image, xofs, yofs, tileWidth, tileHeight, xpad, ypad, -1, -1);
	}
	
	public AtlasImage of(ClientImage image, int xofs, int yofs, int tileWidth, int tileHeight, int xpad, int ypad, int tilesWide, int tilesHigh) {
		AtlasImage result = new AtlasImage();
		result.image = image;
		result.addIsland(xofs, yofs, tileWidth, tileHeight, xpad, ypad, tilesWide, tilesHigh);
		return result;
	}
	
	public Tile getTile(int index) {
		return tiles.get(index);
	}
	
	public ClientImage getAsImage(int index) {
		Tile t = getTile(index);
		ClientImage result = new ClientImage(t.getWidth(), t.getHeight());
		for(int y=0; y<t.getWidth(); y++) {
			for(int x=0; x<t.getHeight(); x++) {
				result.setPixel(x, y, image.getPixel(x+t.getX(), y+t.getY()));
			}
		}
		return result;
	}
	
	public Iterator<Tile> iterator() {
		return tiles.iterator();
	}
	
	public static class Tile {
		private int x;
		private int y;
		private int width;
		private int height;
		
		public int getX() { return x; }
		public int getY() { return y; }
		public int getWidth() { return width; }
		public int getHeight() { return height; }
		
		public Tile(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}
}
