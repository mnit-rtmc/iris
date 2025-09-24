/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2025  Minnesota Department of Transportation
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
public class BitmapGraphic extends RasterGraphic {

	/** Create a new bitmap graphic */
	public BitmapGraphic(int w, int h) {
		super(w, h);
	}

	/** Get the pixel data length in bytes */
	@Override
	public int length() {
		return (width * height + 7) / 8;
	}

	/** Get the pixel index for the specified location */
	private int pixelIndex(int x, int y) {
		if (x < 0 || x >= width) {
			throw new IndexOutOfBoundsException("x=" + x +
				", width=" + width);
		}
		if (y < 0 || y >= height) {
			throw new IndexOutOfBoundsException("y=" + y +
				", height=" + height);
		}
		return (y * width) + x;
	}

	/** Check if a specified pixel is transparent */
	@Override
	public boolean isTransparent(int x, int y) {
		int p = pixelIndex(x, y);
		int by = p / 8;
		int bi = 7 - (p % 8);
		return ((pixels[by] >> bi) & 1) == 0;
	}

	/** Get the pixel color at the specified location */
	@Override
	public DmsColor getPixel(int x, int y) {
		return getPixel(x, y, DmsColor.AMBER);
	}

	/** Get the pixel color at the specified location */
	@Override
	public DmsColor getPixel(int x, int y, DmsColor fg) {
		return isTransparent(x, y) ? DmsColor.BLACK : fg;
	}

	/** Set the pixel color at the specified location */
	@Override
	public void setPixel(int x, int y, DmsColor clr) {
		int p = pixelIndex(x, y);
		int by = p / 8;
		int bi = 1 << (7 - (p % 8));
		if (clr.isLit())
			pixels[by] |= bi;
		else
			pixels[by] &= bi ^ 0xff;
	}

	/** Set all pixels adjacent to lit pixels (clearing lit pixels) */
	public void outlineLitPixels() {
		BitmapGraphic bg = createBlankCopy();
		bg.copy(this);
		// Set neighbors of all lit pixels
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (!bg.isTransparent(x, y))
					setNeighbors(x, y);
			}
		}
		// Clear all original lit pixels
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (!bg.isTransparent(x, y))
					setPixel(x, y, DmsColor.BLACK);
			}
		}
	}

	/** Set the neighbors of the specified pixel */
	private void setNeighbors(int x, int y) {
		int xmin = Math.max(x - 1, 0);
		int xmax = Math.min(x + 2, width);
		int ymin = Math.max(y - 1, 0);
		int ymax = Math.min(y + 2, height);
		for (int xx = xmin; xx < xmax; xx++) {
			for (int yy = ymin; yy < ymax; yy++)
				setPixel(xx, yy, DmsColor.AMBER);
		}
	}

	/** Clear all pixels with no lit neighbor pixels */
	public void clearNoLitNeighbors() {
		BitmapGraphic bg = createBlankCopy();
		bg.copy(this);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (bg.countLitNeighbors(x, y) == 0)
					setPixel(x, y, DmsColor.BLACK);
			}
		}
	}

	/** Count lit neighbors of a specified pixel */
	private int countLitNeighbors(int x, int y) {
		int lit = 0;
		int xmin = Math.max(x - 1, 0);
		int xmax = Math.min(x + 2, width);
		int ymin = Math.max(y - 1, 0);
		int ymax = Math.min(y + 2, height);
		for (int xx = xmin; xx < xmax; xx++) {
			for (int yy = ymin; yy < ymax; yy++) {
				if (xx == x && yy == y)
					continue;
				if (!isTransparent(xx, yy))
					lit++;
			}
		}
		return lit;
	}

	/** Update by intersection with another graphic */
	public void intersection(BitmapGraphic bg) {
		if (width != bg.width)
			throw new IndexOutOfBoundsException("width mismatch");
		if (height != bg.height)
			throw new IndexOutOfBoundsException("height mismatch");
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (bg.isTransparent(x, y))
					setPixel(x, y, DmsColor.BLACK);
			}
		}
	}

	/** Create a blank copy */
	public BitmapGraphic createBlankCopy() {
		return new BitmapGraphic(width, height);
	}
}
