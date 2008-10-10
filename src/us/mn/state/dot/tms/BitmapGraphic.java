/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms;

import java.io.Serializable;

/**
 * BitmapGraphic is a simple 1-bit graphic bitmap for DMS display feedback.
 *
 * @author Douglas Lau
 */
public class BitmapGraphic implements Serializable {

	/** Width of graphic */
	public final int width;

	/** Height of graphic */
	public final int height;

	/** Bitmap graphic data */
	protected final byte[] bitmap;

	/** Create a new bitmap graphic */
	public BitmapGraphic(int w, int h) {
		width = w;
		height = h;
		bitmap = new byte[(width * height + 7) / 8];
	}

	/** Set the bitmap */
	public void setBitmap(byte[] b) {
		if(b.length != bitmap.length)
			throw new IndexOutOfBoundsException("b="+b+
			", bitmap.length="+bitmap.length);
		System.arraycopy(b, 0, bitmap, 0, bitmap.length);
	}

	/** Get the bitmap */
	public byte[] getBitmap() {
		return bitmap;
	}

	/** Get the bitmap length in bytes */
	public int length() {
		return (bitmap==null ? 0 : bitmap.length);
	}

	/** Get the pixel index for the specified location */
	protected int pixelIndex(int x, int y) {
		if(x < 0 || x > width)
			throw new IndexOutOfBoundsException("x="+x+", width="+width);
		if(y < 0 || y > height)
			throw new IndexOutOfBoundsException("y="+y+", height="+height);
		return (y * width) + x;
	}

	/** Get the pixel value at the specified location */
	public int getPixel(int x, int y) {
		int p = pixelIndex(x, y);
		int by = p / 8;
		int bi = 7 - (p % 8);
		return (bitmap[by] >> bi) & 1;
	}

	/** Set the pixel value at the specified location */
	public void setPixel(int x, int y, int value) {
		int p = pixelIndex(x, y);
		int by = p / 8;
		int bi = 1 << (7 - (p % 8));
		if(value > 0)
			bitmap[by] |= bi;
		else
			bitmap[by] &= bi ^ 0xff;
	}

	/** Copy the common region of the specified bitmap */
	public void copy(BitmapGraphic b) {
		int w = Math.min(width, b.width);
		int h = Math.min(height, b.height);
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++)
				setPixel(x, y, b.getPixel(x, y));
		}
	}

	/** Return a wider bitmap. The existing bitmap is centered within
	 *  the new wider bitmap.
	 *  @param newwidth The new width of the bitmap.
	 *  @param newfill Bit value of the new space.
	 */
	public BitmapGraphic widen(int newwidth, int newfill) {
		if(newwidth<0 || newwidth <= width)
			return this;

		// new bitmap
		BitmapGraphic newbm = new BitmapGraphic(newwidth,this.height);

		// edges of existing bitmap centered within wider bitmap
		int ledgeidx = (newbm.width - this.width)/2;
		int redgeidx = newbm.width - ledgeidx - 1;
		assert ledgeidx > 0;
		assert redgeidx > 0;
		assert ledgeidx <= redgeidx;
		// insert existing bitmap into wider bitmap
		for(int y=0; y<newbm.height; ++y)
			for(int x=0; x<newbm.width; ++x) {
				int newpix = newfill;
				if(x >= ledgeidx && x <= redgeidx)
					newpix = getPixel(x - ledgeidx, y);
				newbm.setPixel(x,y,newpix);
			}
		return newbm;
	}
}

