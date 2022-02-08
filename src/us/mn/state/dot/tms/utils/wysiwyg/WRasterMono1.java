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

/** WRasterMono1: WRaster child-class for 
 *  mono1bit graphics.
 * 
 * Each value in the pixels array for this class
 * contains a single off/on pixel value or one
 * of the three generic DEFAULT_BG, DEFAULT_FG,
 * or ERROR_PIXEL colors.
 * (value range: -3..1)
 * 
 * @author John L. Stanley - SRF Consulting
 */
public class WRasterMono1 extends WRaster {

	@Override
	protected void setDefaults() {
		this.colorscheme = ColorScheme.MONOCHROME_1_BIT;
		this.max_pixel   = 1;
		this.max_taglen  = 1;
		this.max_tagitem = 1;
	}

	/** Constructor for WRasterMono1
	 * @param mcfg MultiConfig used to configure the graphic
	 */
	protected WRasterMono1(MultiConfig mcfg) {
		super(mcfg);
	}

	/** Constructor for simple WRasterMono1 from width &amp; height.
	 * (Used for cloning a WRaster, WGlyph and NTCIP pixel-error bitmaps.) */
	protected WRasterMono1(int width, int height) {
		super(width, height);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#length()
	 */
	@Override
	public int length() {
		return ((width * height) + 7) / 8;
	}

	//---------------------------------
	// color conversions

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#tagvalToPixel2(int[])
	 */
	@Override
	protected Integer tagvalToPixel2(int[] tagval) {
		if (tagval.length != 1)
			return null;
		int pix = tagval[0];
		return (pix == 0)|(pix == 1) ? pix : null;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#getColor(int, int)
	 */
	@Override
	protected DmsColor pixelToColor(int pixel) {
		if (isLit(pixel))
			return defaultFG;
		return defaultBG;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#classicColorToPixel(int)
	 */
	@SuppressWarnings("incomplete-switch")
	@Override
	public Integer classicColorToPixel(int cco) {
		switch (ColorClassic.fromOrdinal(cco)) {
			case black:
				return 0;
			case white:
				return 1;
			case amber:
				// Make accommodation for common mistake...
				return 1;
		}
		return null;
	}

	//---------------------------------
	// NTCIP bitmap methods
	
	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#setPixelData(byte[])
	 */
	@Override
	public void setPixelData(byte[] ba) {
		assertValidDmsGraphicByteArray(ba);
		int ind, by, bi;
		for (int x = 0; (x < width); ++x) {
			for (int y = 0; (y < height); ++y) {
				ind = pixelIndex(x, y);
				by = ind / 8;
				bi = 7 - (ind % 8);
				if (((ba[by] >> bi) & 1) == 0)
					pixels[ind] = 0;
				else
					pixels[ind] = 1;
			}
		}
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#getPixelData()
	 */
	@Override
	public byte[] getPixelData() {
		byte[] ba = new byte[length()];
		int ind, by, bi;
		for (int x = 0; (x < width); ++x) {
			for (int y = 0; (y < height); ++y) {
				ind = pixelIndex(x, y);
				by = ind / 8;
				bi = 7 - (ind % 8);
				if (pixels[ind] != 0)
					ba[by] |= (1 << bi);
			}
		}
		return ba;
	}
}
