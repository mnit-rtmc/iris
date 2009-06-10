/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
 * RasterGraphic is a raster graphic for DMS display feedback.
 *
 * @author Douglas Lau
 */
abstract public class RasterGraphic {

	/** Width of raster graphic */
	protected final int width;

	/** Get the raster graphic width */
	public int getWidth() {
		return width;
	}

	/** Height of raster graphic */
	protected final int height;

	/** Get the raster graphic height */
	public int getHeight() {
		return height;
	}

	/** Pixel data */
	protected final byte[] pixels;

	/** Create a new raster graphic */
	protected RasterGraphic(int w, int h) {
		width = w;
		height = h;
		pixels = new byte[length()];
	}

	/** Set the pixel data */
	public void setPixels(byte[] p) {
		if(p.length != length()) {
			throw new IndexOutOfBoundsException("p=" + p.length +
				", length=" + length());
		}
		System.arraycopy(p, 0, pixels, 0, pixels.length);
	}

	/** Get the pixel data */
	public byte[] getPixels() {
		return pixels;
	}

	/** Get the pixel data length in bytes */
	abstract public int length();

	/** Get the pixel value at the specified location */
	abstract public int getPixel(int x, int y);

	/** Set the pixel value at the specified location */
	abstract public void setPixel(int x, int y, int value);

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

	/** Copy the common region of the specified raster */
	public void copy(RasterGraphic b) {
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
}
