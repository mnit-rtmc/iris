/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2015  Minnesota Department of Transportation
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
 * PixmapGraphic is a simple 24-bit graphic pixmap for DMS display feedback.
 * Pixel data is stored in BGR order.
 *
 * @author Douglas Lau
 */
public class PixmapGraphic extends RasterGraphic {

	/** Create a new pixmap graphic */
	public PixmapGraphic(int w, int h) {
		super(w, h);
	}

	/** Get the pixel data length in bytes */
	@Override
	public int length() {
		return width * height * 3;
	}

	/** Get the pixel index for the specified location */
	private int pixelIndex(int x, int y) {
		if (x < 0 || x > width) {
			throw new IndexOutOfBoundsException("x=" + x +
				", width=" + width);
		}
		if (y < 0 || y > height) {
			throw new IndexOutOfBoundsException("y=" + y +
				", height=" + height);
		}
		return ((y * width) + x) * 3;
	}

	/** Get the pixel color at the specified location */
	@Override
	public DmsColor getPixel(int x, int y) {
		int p = pixelIndex(x, y);
		int blue = pixels[p + 0] & 0xFF;
		int green = pixels[p + 1] & 0xFF;
		int red = pixels[p + 2] & 0xFF;
		return new DmsColor(red, green, blue);
	}

	/** Set the pixel color at the specified location */
	@Override
	public void setPixel(int x, int y, DmsColor clr) {
		int p = pixelIndex(x, y);
		pixels[p + 0] = (byte)clr.blue;
		pixels[p + 1] = (byte)clr.green;
		pixels[p + 2] = (byte)clr.red;
	}
}
