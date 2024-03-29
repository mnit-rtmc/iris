/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2022  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.utils.Base64;

/**
 * RasterGraphic is a raster graphic for DMS display feedback.
 *
 * @author Douglas Lau
 */
abstract public class RasterGraphic {

	/** Factory to create raster graphics */
	public interface Factory {
		RasterGraphic create();
	}

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
	public void setPixelData(byte[] p) {
		if (p.length != length()) {
			throw new IndexOutOfBoundsException("p=" + p.length +
				", length=" + length());
		}
		System.arraycopy(p, 0, pixels, 0, pixels.length);
	}

	/** Get the pixel data */
	public byte[] getPixelData() {
		return pixels;
	}

	/** Get pixel data enocded to Base64 */
	public String getEncodedPixels() {
		return Base64.encode(pixels);
	}

	/** Get the pixel data length in bytes */
	abstract public int length();

	/** Check if a specified pixel is transparent */
	abstract public boolean isTransparent(int x, int y);

	/** Get the pixel color at the specified location */
	abstract public DmsColor getPixel(int x, int y);

	/** Get the pixel color at the specified location */
	public DmsColor getPixel(int x, int y, DmsColor fg) {
		return getPixel(x, y);
	}

	/** Set the pixel color at the specified location */
	abstract public void setPixel(int x, int y, DmsColor clr);

	/** Get the count of lit pixels */
	public int getLitCount() {
		int n_lit = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (getPixel(x, y).isLit())
					n_lit++;
			}
		}
		return n_lit;
	}

	/** Copy the common region of the specified raster */
	public void copy(RasterGraphic rg) {
		int x0 = Math.max(width - rg.width, 0) / 2;
		int x1 = Math.max(rg.width - width, 0) / 2;
		int y0 = Math.max(height - rg.height, 0) / 2;
		int y1 = Math.max(rg.height - height, 0) / 2;
		int w = Math.min(width, rg.width);
		int h = Math.min(height, rg.height);
		for (int x = 0; x < w; x++) {
			int xx = x1 + x;
			for (int y = 0; y < h; y++) {
				int yy = y1 + y;
				if (!rg.isTransparent(xx, yy)) {
					DmsColor clr = rg.getPixel(xx, yy);
					setPixel(x0 + x, y0 + y, clr);
				}
			}
		}
	}

	/** Copy another raster graphic onto the raster.
	 * @param rg RasterGraphic to copy.
	 * @param x0 X-position on raster (0-based).
	 * @param y0 Y-position on raster (0-based).
	 * @param fg Foreground color. */
	public void copy(RasterGraphic rg, int x0, int y0, DmsColor fg) {
		int w = rg.getWidth();
		int h = rg.getHeight();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (!rg.isTransparent(x, y)) {
					DmsColor clr = rg.getPixel(x, y, fg);
					setPixel(x0 + x, y0 + y, clr);
				}
			}
		}
	}
}
