package com.playsawdust.chipper.glow.util;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import com.google.common.base.Preconditions;

/** Not a joml class. */
public class RectangleI {
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	
	public RectangleI(int x, int y, int width, int height) {
		Preconditions.checkArgument(width>=0, "Width can't be negative");
		Preconditions.checkArgument(height>=0, "Height can't be negative");
		
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public RectangleI(RectangleI other) {
		Preconditions.checkArgument(other.width>=0, "Width can't be negative");
		Preconditions.checkArgument(other.height>=0, "Height can't be negative");
		
		this.x = other.x;
		this.y = other.y;
		this.width = other.width;
		this.height = other.height;
	}
	
	public int x() {
		return x;
	}
	
	public int y() {
		return y;
	}
	
	public int width() {
		return width;
	}
	
	public int height() {
		return height;
	}
	
	public int getLeft() {
		return x;
	}
	
	/** Gets the X coordinate of the last pixel within the right edge of this Rectangle* */
	public int getRight() {
		return x+width-1;
	}
	
	public int getTop() {
		return y;
	}
	
	/** Gets the Y coordinate of the last pixel within the bottom edge of this RectangleI */
	public int getBottom() {
		return y+height-1;
	}
	
	public boolean intersects(RectangleI other) {
		return
				this.x  < other.x + other.width  &&
				other.x < this.x  + this.width   &&
				this.y  < other.y + other.height &&
				other.y < this.y  + this.height;
	}
	
	public boolean intersects(int x, int y, int width, int height) {
		return
				this.x < x + width &&
				x      < this.x + this.width &&
				this.y < y + height &&
				y      < this.y + this.height;
	}
	
	/** Returns true if the specified point falls inside this RectangleI's area, including its edges. */
	public boolean contains(int x, int y) {
		return
			x >= this.x &&
			x < this.x+this.width &&
			y >= this.y &&
			y < this.y + this.width;
	}
	
	public boolean contains(Vector2ic vec) {
		return contains(vec.x(), vec.y());
	}
	
	public void set(RectangleI other) {
		this.x = other.x;
		this.y = other.y;
		this.width = other.width;
		this.height = other.height;
	}
	
	public void set(int x, int y, int width, int height) {
		Preconditions.checkArgument(width>=0, "Width can't be negative");
		Preconditions.checkArgument(height>=0, "Height can't be negative");
		
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void setSize(int width, int height) {
		Preconditions.checkArgument(width>=0, "Width can't be negative");
		Preconditions.checkArgument(height>=0, "Height can't be negative");
		
		this.width = width;
		this.height = height;
	}
	
	public void setLeft(int x) {
		this.x = x;
	}
	
	public void setTop(int y) {
		this.y = y;
	}
	
	/** Preserves width but places the last pixel of this RectangleI at the specified X value. For example, if a 16x16 box was asked to setRight(16), it would move to x=1. */
	public void setRight(int x) {
		this.x = x - (width-1);
	}
	
	/** Preserves height but places the last pixel of this RectangleI at the specified Y value. For example, if a 16x16 box was asked to setBottom(16), it would move to y=1. */
	public void setBottom(int y) {
		this.y = y - (height-1);
	}
	
	/** Causes this RectangleI to expand such that contains(x, y) will return true */
	public void expandTo(int x, int y) {
		if (x<this.x) this.x = x;
		if (y<this.y) this.y = y;
		if (this.x+this.width>=x) this.width = x + 1 - this.x;
		if (this.y+this.height>=y) this.height = y + 1 - this.y;
	}
	
	/** Returns true if and only if this RectangleI has zero area */
	public boolean isEmpty() {
		return width==0 || height==0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj==null) return false;
		if (getClass()!=obj.getClass()) return false;
		
		RectangleI other = (RectangleI)obj;
		return
				this.x==other.x &&
				this.y==other.y &&
				this.width==other.width &&
				this.height==other.height;
	}
	
	@Override
	public int hashCode() {
		int result = 7;
		result = result * 31 + Integer.hashCode(x);
		result = result * 31 + Integer.hashCode(y);
		result = result * 31 + Integer.hashCode(width);
		result = result * 31 + Integer.hashCode(height);
		return result;
	}
}
