/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.RasterBuilder;

/**
 * Text rectangle on a full-matrix sign
 *
 * @author Douglas Lau
 */
public class TextRect {
	public final int page_number;
	public final int width;
	public final int height;
	public final int font_num;

	/** Create a new text rectangle */
	public TextRect(int pn, int w, int h, int fn) {
		page_number = pn;
		width = w;
		height = h;
		font_num = fn;
	}

	/** Get the number of lines of text on the rectangle */
	public int getLineCount() {
		// color scheme doesn't matter here
		RasterBuilder rb = new RasterBuilder(width, height, 0, 0,
			font_num, ColorScheme.COLOR_24_BIT);
		return rb.getLineCount();
	}
}
