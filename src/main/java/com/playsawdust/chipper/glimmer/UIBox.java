package com.playsawdust.chipper.glimmer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blue.endless.splinter.LayoutContainer;
import blue.endless.splinter.LayoutContainerMetrics;
import blue.endless.splinter.LayoutElement;
import blue.endless.splinter.LayoutElementMetrics;

public class UIBox extends UIElement implements LayoutContainer {
	protected LayoutContainerMetrics metrics = new LayoutContainerMetrics()
			.setCellPadding(4)
			.setCollapseMargins(true);
	protected Orientation orientation = Orientation.VERTICAL;
	protected List<UIElement> children = new ArrayList<>();
	protected Map<UIElement, LayoutElementMetrics> layout = new HashMap<>();
	
	public UIBox(Orientation orientation) {
		this.orientation = orientation;
	}
	
	public void add(UIElement child) {
		children.add(child);
		int x = 0;
		int y = 0;
		if (orientation==Orientation.VERTICAL) {
			y = children.size()-1;
		} else {
			x = children.size()-1;
		}
		layout.put(child, new LayoutElementMetrics(x,y));
	}
	
	//implements LayoutContainer {
		@Override
		public Iterable<? extends LayoutElement> getLayoutChildren() {
			return children;
		}
	
		@Override
		public LayoutElementMetrics getLayoutElementMetrics(LayoutElement elem) {
			return layout.get(elem);
		}
	
		@Override
		public LayoutContainerMetrics getLayoutContainerMetrics() {
			return metrics;
		}
	
		@Override
		public void setLayoutValues(LayoutElement elem, int x, int y, int width, int height) {
			if (elem instanceof UIElement) {
				UIElement ui = (UIElement) elem;
				ui.x = x;
				ui.y = y;
				ui.width = width;
				ui.height = height;
			}
		}
	//}
	
	public enum Orientation {
		VERTICAL,
		HORIZONTAL;
	}
}
