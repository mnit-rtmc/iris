/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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

package us.mn.state.dot.tms.utils.wysiwyg;

import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.utils.ColorClassic;
import us.mn.state.dot.tms.utils.MultiConfig;

/** WRaster child-class for 8-bit monochrome graphics.
 * 
 * Each value in the pixels array for this class
 * contains a pixel brightness value or one
 * of the three generic DEFAULT_BG, DEFAULT_FG,
 * or ERROR_PIXEL colors
 * (range: -3..255).
 * 
 * @author John L. Stanley - SRF Consulting
 */
public class WRasterMono8 extends WRaster {

	@Override
	protected void setDefaults() {
		this.colorscheme = ColorScheme.MONOCHROME_8_BIT;
		this.max_pixel   = 255;
		this.max_taglen  = 1;
		this.max_tagitem = 255;
	}

	/** Constructor for WRasterMono8
	 * @param mcfg MultiConfig used to configure the graphic
	 */
	protected WRasterMono8(MultiConfig mcfg) {
		super(mcfg);
	}

	/** Constructor for simple WRasterMono8 from width &amp; height.
	 *  (Used when cloning a WRaster.) */
	protected WRasterMono8(int width, int height) {
		super(width, height);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#length()
	 */
	@Override
	public int length() {
		return width * height;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#pixelToColor(int)
	 */
	@Override
	protected DmsColor pixelToColor(int pixel) {
		DmsColor c = defaultFG;
		int r = (c.red   * pixel) >> 8;
		int g = (c.green * pixel) >> 8;
		int b = (c.blue  * pixel) >> 8;
		return new DmsColor(r, g, b);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#setPixelData(byte[])
	 */
	@Override
	public void setPixelData(byte[] ba) {
		assertValidDmsGraphicByteArray(ba);
		int len = length();
		for (int ind = 0; (ind < len); ++ind)
			pixels[ind] = ba[ind] & 0x0ff;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#getPixelData()
	 */
	@Override
	public byte[] getPixelData() {
		int len = length();
		byte[] ba = new byte[len];
		for (int ind = 0; (ind < len); ++ind)
			ba[ind] = (byte) pixels[ind];
		return ba;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#tagToPix(int[])
	 */
	@Override
	protected Integer tagvalToPixel2(int[] tagval) {
		if (tagval.length != 1)
			return null;
		int pix = tagval[0];
		return ((pix >= 0) && (pix <= 255)) ? pix : null;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#classicColorToPixel(int)
	 */
	@SuppressWarnings("incomplete-switch")
	@Override
	public Integer classicColorToPixel(int cco) {
		switch (ColorClassic.fromOrdinal(cco)) {
			case black:
				return WRaster.BLACK;
			case white:
				return 255;
			case amber:
				// Make accommodation for common mistake...
				return 255;
		}
		return null;
	}
}
