package com.playsawdust.chipper.glow.image;

import java.util.ArrayList;

import com.playsawdust.chipper.glow.util.RectangleI;

public class ShelfStitcher implements Stitcher {
	
	protected int shelfHeight;
	protected int totalHeight = 0;
	protected int curPowerOf2 = 32;
	protected int maxSize;
	protected ArrayList<Shelf> shelves = new ArrayList<>();
	
	public ShelfStitcher(int minShelfHeight, int maxSize) {
		this.shelfHeight = minShelfHeight;
		this.maxSize = maxSize;
		this.curPowerOf2 = 4;
	}

	@Override
	public boolean stitch(RectangleI rect) {
		//Try existing shelves
		for(Shelf shelf : shelves) {
			if (shelf.canFit(rect, curPowerOf2)) {
				shelf.add(rect);
				return true;
			}
		}
		
		// Can we fit a new shelf?
		int proposedShelfSize = Math.max(rect.height(), shelfHeight);
		if (totalHeight+proposedShelfSize <= curPowerOf2) {
			Shelf shelf = new Shelf();
			shelf.y = totalHeight;
			shelf.height = proposedShelfSize;
			shelves.add(shelf);
			
			totalHeight += shelf.height;
			
			shelf.add(rect);
			
			//System.out.println("STITCHER: New regular shelf with height "+shelf.height);
			
			return true;
		} else {
			//See if we can squeeze in a last row which is smaller than minShelfHeight
			if (totalHeight+rect.height() <= curPowerOf2) {
				Shelf shelf = new Shelf();
				shelf.y = totalHeight;
				shelf.height = rect.height();
				shelves.add(shelf);
				
				totalHeight += shelf.height;
				
				shelf.add(rect);
				
				//System.out.println("STITCHER: New \"short\" shelf with height "+shelf.height);
				
				return true;
			}
		}
		
		//Is expanding by a powerOf2 possible, and is it likely to fit the new rect in?
		if (curPowerOf2 * 2 <= maxSize) {
			//System.out.println("STITCHER: Resize "+curPowerOf2+" -> "+(curPowerOf2*2));
			curPowerOf2 *= 2;
			//Then we'll probably only go one stack level deeper.
			return stitch(rect);
		}
		
		return false;
	}
	
	public static class Shelf {
		int y;
		int height;
		int width;
		
		public boolean canFit(RectangleI rect, int widthLimit) {
			return
				rect.height()<=height &&
				width+rect.width()<=widthLimit;
		}
		
		public void add(RectangleI rect) {
			rect.setLeft(width);
			rect.setTop(y);
			width += rect.width();
		}
	}
}
