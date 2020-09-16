package com.playsawdust.chipper.glow.text.truetype.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.collect.ClassToInstanceMap;
import com.playsawdust.chipper.glow.image.BlendMode;
import com.playsawdust.chipper.glow.image.ImageData;
import com.playsawdust.chipper.glow.image.ImageDataEditor;
import com.playsawdust.chipper.glow.image.ImageEditor;
import com.playsawdust.chipper.glow.text.truetype.TTFDataInput;
import com.playsawdust.chipper.glow.util.Contour;
import com.playsawdust.chipper.glow.util.VectorShape;

import blue.endless.jankson.JsonObject;

public class TTFGlyph extends TTFTable {
	public static final String TAG_NAME = "glyf";
	public static final int TAG = tagNameToInt(TAG_NAME);
	
	public TTFDataInput glyphData;
	
	public TTFGlyph(int offset, int length) { super(offset, length); }

	@Override
	public void load(TTFDataInput data, ClassToInstanceMap<TTFTable> tables) throws IOException {
		byte[] glyphDataArray = new byte[this.getLength()];
		for(int i=0; i<glyphDataArray.length; i++) {
			glyphDataArray[i] = data.readByte();
		}
		
		glyphData = new TTFDataInput(glyphDataArray);
		//TODO: Slice this instead, or arraycopy it.
		
		
		//readGlyph(0);
		
	}
	
	public GlyphData readGlyph(int offset) {
		glyphData.seek(offset);
		
		GlyphData result = new GlyphData();
		int numberOfContours = glyphData.readInt16();
		result.xMin = glyphData.readFWord();
		result.yMin = glyphData.readFWord();
		result.xMax = glyphData.readFWord();
		result.yMax = glyphData.readFWord();
		
		if (numberOfContours==0) {
			//No contours
			return result;
		} else if (numberOfContours>0) {
			int[] endPointsOfContours = new int[numberOfContours];
			for(int i=0; i<numberOfContours; i++) endPointsOfContours[i] = glyphData.readUInt16();
			int instructionLength = glyphData.readUInt16();
			byte[] instructions = new byte[instructionLength];
			for(int i=0; i<instructionLength; i++) instructions[i] = glyphData.readByte();
			
			//We discover the point count by examining the last (highest) index of the "endPointsOfContours" list. The list of points is therefore indexed [0..lastPointIndex] and its size is lastPointIndex+1
			int pointCount = endPointsOfContours[endPointsOfContours.length-1]+1;
			
			//There are that many flags.
			byte[] flags = new byte[pointCount];
			for(int i=0; i<pointCount; i++) {
				byte flag = glyphData.readByte();
				flags[i] = flag;
				if((flags[i] & 0x08) != 0) {
					//handle repeats!
					int count = glyphData.readByte();
					for(int j=0; j<count; j++) {
						i += 1;
						flags[i] = flag;
					}
				}
			}
			
			//unpack coordinate arrays
			
			int[] xCoordinates = new int[pointCount];
			for(int i=0; i<pointCount; i++) {
				int xPointSize = flags[i] & 0x02;
				
				int rawX = 0;
				if (xPointSize==0) {
					boolean sameX = (flags[i] & 0x10) != 0;
					
					if (sameX) {
						rawX = (i==0) ? 0 : xCoordinates[i-1];
					} else {
						rawX = glyphData.readInt16();
						if (i>0) rawX = xCoordinates[i-1] + rawX;
					}
				} else {
					rawX = glyphData.readUByte();
					boolean signX = (flags[i] & 0x10) != 0;
					if (!signX) rawX = -rawX;
					
					if (i>0) rawX = xCoordinates[i-1] + rawX;
				}
				
				xCoordinates[i] = rawX;
			}
			
			int[] yCoordinates = new int[pointCount];
			for(int i=0; i<pointCount; i++) {
				int yPointSize = flags[i] & 0x04;
				
				int rawY = 0;
				if (yPointSize==0) {
					boolean sameY = (flags[i] & 0x20) != 0;
					
					if (sameY) {
						rawY = (i==0) ? 0 : yCoordinates[i-1];
					} else {
						rawY = glyphData.readInt16();
						if (i>0) rawY = yCoordinates[i-1] + rawY;
					}
				} else {
					rawY = glyphData.readUByte();
					boolean signY = (flags[i] & 0x20) != 0;
					if (!signY) rawY = -rawY;
					
					if (i>0) rawY = yCoordinates[i-1] + rawY;
				}
				
				yCoordinates[i] = rawY;
			}
			
			//Convert from TTF arrays to points
			
			GlyphPoint[] indexedPoints = new GlyphPoint[pointCount];
			for(int i=0; i<pointCount; i++) {
				indexedPoints[i] = new GlyphPoint();
				indexedPoints[i].onContour = (flags[i] & 0x01) != 0;
				indexedPoints[i].x = xCoordinates[i];
				indexedPoints[i].y = yCoordinates[i];
			}
			
			//Organize points into contours, and add implied intermediate points if they're missing (such as inflection points between two off-curve contours)
			
			int contourStart = 0;
			for(int i=0; i<endPointsOfContours.length; i++) {
				int contourEnd = endPointsOfContours[i];
				if (contourStart>contourEnd) continue;
				TTFContour contour = new TTFContour();
				GlyphPoint lastPoint = null;
				for(int pointIndex=contourStart; pointIndex<=contourEnd; pointIndex++) {
					if (pointIndex<0 || pointIndex>=indexedPoints.length) {
						continue; //Should never happen
					}
					GlyphPoint nextPoint = indexedPoints[pointIndex].copy();
					
					if (lastPoint!=null && !lastPoint.onContour && !nextPoint.onContour) {
						//There is an implied on-contour point between these points
						GlyphPoint intermediate = new GlyphPoint();
						intermediate.onContour = true;
						intermediate.x = (lastPoint.x+nextPoint.x)/2;
						intermediate.y = (lastPoint.y+nextPoint.y)/2;
						contour.points.add(intermediate);
					}
					
					contour.points.add(nextPoint);
					lastPoint = nextPoint;
				}
				if (contour.points.size()>0) {
					GlyphPoint firstPoint = contour.points.get(0);
					if (!lastPoint.onContour && !firstPoint.onContour) {
						//Emit one last implied intermediate
						GlyphPoint intermediate = new GlyphPoint();
						intermediate.onContour = true;
						intermediate.x = (lastPoint.x+firstPoint.x)/2;
						intermediate.y = (lastPoint.y+firstPoint.y)/2;
						contour.points.add(intermediate);
					}
				}
				
				result.contours.add(contour);
				System.out.println(contour.points.toString());
				contourStart = contourEnd+1;
			}
			
			//Convert from points to Glow contour segments
			
			VectorShape vectorShape = new VectorShape();
			for(TTFContour contour : result.contours) {
				Contour vectorContour = new Contour();
				GlyphPoint a = null;
				GlyphPoint b = null;
				for(GlyphPoint point : contour.points) {
					GlyphPoint c = point;
					if (b!=null && c.x == b.x && c.y==b.y && c.onContour==b.onContour) continue;
					
					//Process points only after the pipe is full enough to do so
					if (b!=null && b.onContour && c.onContour) {
						//Simple line segment
						vectorContour.addBoundarySegment(new Contour.LineSegment(b.x, b.y, c.x, c.y));
					/*} else if (b!=null && !b.onContour && !c.onContour) {
						//Found two consecutive off-contour points. Create a synthetic point between them!
						//TODO: Previous steps probably take care of this!
						a=b;
						b = new GlyphPoint();
						b.onContour = true;
						b.x = (a.x+b.x)/2;
						b.y = (a.y+b.y)/2;*/
					} else if (a!=null && a.onContour && b!=null && !b.onContour && c.onContour) {
						vectorContour.addBoundarySegment(new Contour.BezierLineSegment(a.x, a.y, b.x, b.y, c.x, c.y));
					}
					
					a=b;
					b=c;
				}
				
				GlyphPoint c = contour.points.get(0); //Close the contour by cycling back to the first point
				if (b!=null && b.onContour && c.onContour) {
					vectorContour.addBoundarySegment(new Contour.LineSegment(b.x, b.y, c.x, c.y));
				} else if (b!=null && b.onContour && !c.onContour) {
					//We actually need to wrap all the way around to point 1
					if (contour.points.size()>1) { //Should always be >=3
						a=b; b=c; c = contour.points.get(1);
						vectorContour.addBoundarySegment(new Contour.BezierLineSegment(a.x, a.y, b.x, b.y, c.x, c.y));
					}
				} else if (a!=null && a.onContour && b!=null && !b.onContour && c.onContour) {
					//TODO: This is getting marked as dead code but I don't see why
					vectorContour.addBoundarySegment(new Contour.BezierLineSegment(a.x, a.y, b.x, b.y, c.x, c.y));
				}
				
				vectorShape.addContour(vectorContour);
			}
			result.shape = vectorShape;
			
			return result;
		} else {
			//compound glyph
			
			int flags = glyphData.readUInt16();
			//int glyphIndex = glyphData.readUInt16();
			//TODO: Read words, bytes, points, whatever based on what's in flags.
			
			if ((flags & 0x10) != 0) {
				//At least one more component follows this one
			}
			return result;
		}
	}

	@Override
	public JsonObject toJson() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public static class GlyphPoint {
		/**
		 * If onContour==false, this is a "hint" point, and the segment between the previous and the next onContour==true point is a quadratic curve tangent to this point.
		 *
		 * <p>To expand on this, between any consecutive onContour==false Points, there is an implied "inflection point" directly at the halfway point between them which
		 * the line positively passes through. So the "prior" point may be just as made-up as the "next" point.
		 */
		boolean onContour;
		int x;
		/**
		 * +y is UP in fonts!
		 */
		int y;
		
		public GlyphPoint copy() {
			GlyphPoint copy = new GlyphPoint();
			copy.onContour = onContour;
			copy.x = x;
			copy.y = y;
			return copy;
		}
		
		@Override
		public String toString() {
			if (onContour) {
				return "Point("+x+", "+y+")";
			} else {
				return "Hint("+x+", "+y+")";
			}
		}
	}
	
	public static class TTFContour {
		ArrayList<GlyphPoint> points = new ArrayList<>();
	}
	
	public static class GlyphData {
		int xMin;
		int yMin;
		int xMax;
		int yMax;
		
		ArrayList<TTFContour> contours = new ArrayList<>();
		VectorShape shape;
		
		public VectorShape getShape() {
			return shape;
		}
		
		/** MAY BEHAVE ERRATICALLY on bitmaps which contain anything except transparency! */
		public void paintOutline(ImageEditor editor, int argb) {
			double sz = Math.max(xMax - xMin, yMax - yMin);
			double scale = 48.0 / sz;
			int xofs = -xMin + 2;
			int yofs = -yMin + (int)sz + 2;
			
			for(TTFContour contour : contours) {
				GlyphPoint a = null;
				GlyphPoint b = null;
				for(GlyphPoint point : contour.points) {
					GlyphPoint c = point;
					
					//Process points only after the pipe is full enough to do so
					if (b!=null && b.onContour && c.onContour) {
						//Simple line segment
						editor.drawLine((b.x+xofs)*scale, (-b.y+yofs)*scale, (c.x+xofs)*scale, (-c.y+yofs)*scale, argb, BlendMode.NORMAL);
					} else if (b!=null && !b.onContour && !c.onContour) {
						//Found two consecutive off-contour points. Create a synthetic point between them!
						a=b;
						b = new GlyphPoint();
						b.onContour = true;
						b.x = (a.x+b.x)/2;
						b.y = (a.y+b.y)/2;
					} else if (a!=null && a.onContour && b!=null && !b.onContour && c.onContour) {
						//editor.drawLine((a.x+xofs)*scale, (-a.y+yofs)*scale, (c.x+xofs)*scale, (-c.y+yofs)*scale, argb, BlendMode.NORMAL);
						editor.drawQuadraticCurve((a.x+xofs)*scale, (-a.y+yofs)*scale, (b.x+xofs)*scale, (-b.y+yofs)*scale, (c.x+xofs)*scale, (-c.y+yofs)*scale, 8, argb, BlendMode.NORMAL);
					}
					
					a=b;
					b=c;
				}
				GlyphPoint c = contour.points.get(0); //Close the contour by cycling back to the first point
				if (b!=null && b.onContour && c!=null && c.onContour) {
					editor.drawLine((b.x+xofs)*scale, (-b.y+yofs)*scale, (c.x+xofs)*scale, (-c.y+yofs)*scale, argb, BlendMode.NORMAL);
				} else if (b!=null && b.onContour && !c.onContour) {
					//We actually need to wrap all the way around to point 1
					if (contour.points.size()>1) { //Should always be >=3
						a=b; b=c; c = contour.points.get(1);
						editor.drawQuadraticCurve((a.x+xofs)*scale, (-a.y+yofs)*scale, (b.x+xofs)*scale, (-b.y+yofs)*scale, (c.x+xofs)*scale, (-c.y+yofs)*scale, 8, argb, BlendMode.NORMAL);
					}
				} else if (a!=null && a.onContour && b!=null && !b.onContour && c.onContour) {
					//editor.drawLine((a.x+xofs)*scale, (-a.y+yofs)*scale, (c.x+xofs)*scale, (-c.y+yofs)*scale, argb, BlendMode.NORMAL);
					editor.drawQuadraticCurve((a.x+xofs)*scale, (-a.y+yofs)*scale, (b.x+xofs)*scale, (-b.y+yofs)*scale, (c.x+xofs)*scale, (-c.y+yofs)*scale, 8, argb, BlendMode.NORMAL);
				}
			}
		}
		
		
		
		public ShapePart shapePart(ImageData data, int x, int y, int outlineTrigger) {
			int pixel = data.getPixel(x, y);
			if (pixel==outlineTrigger) return ShapePart.BORDER;
			
			//Draw a line to the left side of the image
			boolean crossingX = false;
			boolean crossingLock = false;
			for(int ix=x; ix>=0; ix--) {
				if (data.getPixel(ix, y)==outlineTrigger) {
					if (!crossingLock) {
						crossingX = !crossingX;
						crossingLock = true;
					}
				} else {
					crossingLock = false;
				}
			}
			
			boolean crossingY = false;
			crossingLock = false;
			for(int iy=y; iy>=0; iy--) {
				if (data.getPixel(x, iy)==outlineTrigger) {
					if (!crossingLock) {
						crossingY = !crossingY;
						crossingLock = true;
					}
				} else {
					crossingLock = false;
				}
			}
			
			if (crossingX & crossingY) return ShapePart.INSIDE;
			else return ShapePart.OUTSIDE;
		}
		
		public void fillOutline(ImageData data, int argbTrigger, int argbFill) {
			for(int y=0; y<data.getHeight(); y++) {
				//boolean paint = false;
				//boolean paintLock = false;
				for(int x=0; x<data.getWidth(); x++) {
					ShapePart part = shapePart(data, x, y, argbTrigger);
					if (part==ShapePart.INSIDE) data.setPixel(x, y, argbFill); 
					/*
					if (data.getPixel(x, y)==argbTrigger) {
						if (!paintLock) {
							paint = !paint;
							paintLock = true;
						}
					} else {
						//Fill or don't fill according to crossings
						if (paint) data.setPixel(x, y, argbFill);
						paintLock = false;
					}*/
				}
			}
		}
	}
	
	public static enum ShapePart {
		OUTSIDE,
		INSIDE,
		BORDER;
	}
}
