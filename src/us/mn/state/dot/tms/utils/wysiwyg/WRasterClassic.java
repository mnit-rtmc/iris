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

/** WRasterClassic: WRaster child-class for 
 *  color-classic graphics.
 * 
 * Each value in the pixels array for this class
 * contains a color-classic color number or one
 * of the three generic DEFAULT_BG, DEFAULT_FG,
 * or ERROR_PIXEL colors.
 * (value range: -3..9)
 * 
 * @author John L. Stanley - SRF Consulting
 */
public class WRasterClassic extends WRaster {

	@Override
	protected void setDefaults() {
		this.colorscheme = ColorScheme.COLOR_CLASSIC;
		this.max_pixel   = ColorClassic.values().length;
		this.max_taglen  = 1;
		this.max_tagitem = ColorClassic.values().length;
	}

	
	/** Constructor for WRasterClassic
	 * @param mcfg MultiConfig used to configure the graphic
	 */
	protected WRasterClassic(MultiConfig mcfg) {
		super(mcfg);
	}

	/** Constructor for simple WRasterClassic from width &amp; height.
	 *  (Used when cloning a WRaster.) */
	protected WRasterClassic(int width, int height) {
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
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#getColor(int, int)
	 */
	@Override
	//DONE
	protected DmsColor pixelToColor(int pixel) {
		ColorClassic cc = ColorClassic.fromOrdinal(pixel);
		if (cc == null)
			throw new IndexOutOfBoundsException("Unknown classic-color: " + pixel);
		return cc.clr;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#setPixelData(byte[])
	 */
	@Override
	public void setPixelData(byte[] ba) {
		assertValidDmsGraphicByteArray(ba);
		int len = length();
		int pixel;
		for (int ind = 0; (ind < len); ++ind) {
			pixel = ba[ind] & 0x0ff;
			assertValidPixel(pixel);
			pixels[ind] = pixel;
		}
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#getPixelData()
	 */
	@Override
	public byte[] getPixelData() {
		int len = length();
		byte[] ba = new byte[len];
		int pixel;
		for (int ind = 0; (ind < len); ++ind) {
			pixel = pixels[ind];
			if (!isValidPixel(pixel)) {
				switch (pixel) {
					case DEFAULT_BG:
						pixel = defaultBgPixel;
						break;
					case DEFAULT_FG:
						pixel = defaultFgPixel;
						break;
					default: // includes ERROR_PIXEL
						pixel = BLACK;
				}
			}
			ba[ind] = (byte) pixel;
		}
		return ba;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#tagvalToPixel2(int[])
	 */
	@Override
	public Integer tagvalToPixel2(int[] tagval) {
		return tagval[0];
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#classicColorToPixel(int)
	 */
	@Override
	public Integer classicColorToPixel(int cco) {
		return cco;
	}
}
