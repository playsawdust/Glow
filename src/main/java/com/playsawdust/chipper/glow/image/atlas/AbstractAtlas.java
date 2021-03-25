package com.playsawdust.chipper.glow.image.atlas;

import java.util.ArrayList;

import com.google.common.base.Preconditions;
import com.playsawdust.chipper.glow.image.ImageData;
import com.playsawdust.chipper.glow.util.RectangleI;

public abstract class AbstractAtlas extends ImageData implements Atlas {
	protected ArrayList<RectangleI> images = new ArrayList<>();
	
	@Override
	public int imageCount() {
		return images.size();
	}
	
	@Override
	public RectangleI getImageRegion(int index) {
		Preconditions.checkElementIndex(index, images.size());
		
		return images.get(index);
	}
	
	public RectangleI[] getImageRegions() {
		return images.toArray(new RectangleI[images.size()]);
	}
	
	@Override
	public ImageData getImage(int index) {
		Preconditions.checkElementIndex(index, images.size());
		
		RectangleI t = getImageRegion(index);
		ImageData result = new ImageData(t.width(), t.height());
		for(int y=0; y<t.width(); y++) {
			for(int x=0; x<t.height(); x++) {
				result.setPixel(x, y, getPixel(x+t.x(), y+t.y()));
			}
		}
		return result;
	}
	
	@Override
	public void replaceImage(int index, ImageData image) {
		Preconditions.checkElementIndex(index, images.size());
		
		RectangleI t = getImageRegion(index);
		for(int y=0; y<t.height(); y++) {
			for(int x=0; x<t.width(); x++) {
				setPixel(x+t.x(), y+t.y(), image.getPixel(x, y));
			}
		}
	}
}
