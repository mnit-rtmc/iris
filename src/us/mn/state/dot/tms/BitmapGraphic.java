/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2009  Minnesota Department of Transportation
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

/**
 * BitmapGraphic is a simple 1-bit graphic bitmap for DMS display feedback.
 *
 * @author Douglas Lau
 */
public class BitmapGraphic {

	/** Width of graphic */
	protected final int width;

	/** Get the raster graphic width */
	public int getWidth() {
		return width;
	}

	/** Height of graphic */
	protected final int height;

	/** Get the raster graphic height */
	public int getHeight() {
		return height;
	}

	/** Bitmap graphic data */
	protected final byte[] bitmap;

	/** Create a new bitmap graphic */
	public BitmapGraphic(int w, int h) {
		width = w;
		height = h;
		bitmap = new byte[(width * height + 7) / 8];
	}

	/** Set the pixel data */
	public void setPixels(byte[] b) {
		if(b.length != bitmap.length) {
			throw new IndexOutOfBoundsException("b=" + b.length +
				", bitmap.length=" + bitmap.length);
		}
		System.arraycopy(b, 0, bitmap, 0, bitmap.length);
	}

	/** Get the pixel data */
	public byte[] getPixels() {
		return bitmap;
	}

	/** Get the bitmap length in bytes */
	public int length() {
		return bitmap.length;
	}

	/** Get the pixel index for the specified location */
	protected int pixelIndex(int x, int y) {
		if(x < 0 || x > width) {
			throw new IndexOutOfBoundsException("x=" + x +
				", width=" + width);
		}
		if(y < 0 || y > height) {
			throw new IndexOutOfBoundsException("y=" + y +
				", height=" + height);
		}
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

	/** Get the count of lit pixels */
	public int getLitCount() {
		int n_lit = 0;
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(getPixel(x, y) > 0)
					n_lit++;
			}
		}
		return n_lit;
	}

	/** Copy the common region of the specified bitmap */
	public void copy(BitmapGraphic b) {
		int x0 = Math.max(width - b.width, 0) / 2;
		int x1 = Math.max(b.width - width, 0) / 2;
		int y0 = Math.max(height - b.height, 0) / 2;
		int y1 = Math.max(b.height - height, 0) / 2;
		int w = Math.min(width, b.width);
		int h = Math.min(height, b.height);
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				int v = b.getPixel(x1 + x, y1 + y);
				setPixel(x0 + x, y0 + y, v);
			}
		}
	}

	/** Update the bitmap by clearing pixels not in another bitmap */
	public void union(BitmapGraphic b) {
		if(width != b.width)
			throw new IndexOutOfBoundsException("width mismatch");
		if(height != b.height)
			throw new IndexOutOfBoundsException("height mismatch");
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(b.getPixel(x, y) == 0)
					setPixel(x, y, 0);
			}
		}
	}

	/** Update the bitmap by clearing pixels in another bitmap */
	public void difference(BitmapGraphic b) {
		if(width != b.width)
			throw new IndexOutOfBoundsException("width mismatch");
		if(height != b.height)
			throw new IndexOutOfBoundsException("height mismatch");
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(b.getPixel(x, y) > 0)
					setPixel(x, y, 0);
			}
		}
	}

	/** Set all pixels adjacent to lit pixels (clearing lit pixels) */
	public void outline() {
		BitmapGraphic b = new BitmapGraphic(width, height);
		b.copy(this);
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(b.getPixel(x, y) > 0)
					setNeighbors(x, y);
			}
		}
		difference(b);
	}

	/** Set the neighbors of the specified pixel */
	protected void setNeighbors(int x, int y) {
		int xmin = Math.max(x - 1, 0);
		int xmax = Math.min(x + 2, width);
		int ymin = Math.max(y - 1, 0);
		int ymax = Math.min(y + 2, height);
		for(int xx = xmin; xx < xmax; xx++) {
			for(int yy = ymin; yy < ymax; yy++)
				setPixel(xx, yy, 1);
		}
	}

	/** Create a blank copy */
	public BitmapGraphic createBlankCopy() {
		return new BitmapGraphic(width, height);
	}
}
