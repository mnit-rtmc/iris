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

/** WRasterColor24: WRaster child-class for 
 *  24bit color graphics.
 * 
 * Each value in the pixels array for this class
 * contains a 24bit RGB color value or one of the
 * three generic DEFAULT_BG, DEFAULT_FG, or
 * ERROR_PIXEL colors.
 * (Range: -3..0x00ffffff)
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WRasterColor24 extends WRaster {

	@Override
	protected void setDefaults() {
		this.colorscheme = ColorScheme.COLOR_24_BIT;
		this.max_pixel   = ((1 << 24) - 1);
		this.max_taglen  = 3;
		this.max_tagitem = 255;
	}

	/** Constructor for WRasterColor24
	 * @param mcfg MultiConfig used to configure the graphic
	 */
	protected WRasterColor24(MultiConfig mcfg) {
		super(mcfg);
	}

	/** Constructor for simple WRasterColor24 from width &amp; height.
	 *  (Used when cloning a WRaster.) */
	protected WRasterColor24(int width, int height) {
		super(width, height);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#length()
	 */
	@Override
	public int length() {
		return width * height * 3;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#tagvalToPixel2(int[])
	 */
	@Override
	protected Integer tagvalToPixel2(int[] tagval) {
		DmsColor c;
		if (tagval.length == 1) {
			ColorClassic cc = ColorClassic.fromOrdinal(tagval[0]);
			c = (cc != null) ? cc.clr : null;
			if (c == null)
				return null;
		}
		else
			c = new DmsColor(tagval[0], tagval[1], tagval[2]);
		return c.rgb() & 0x0ffffff;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#pixelToColor(int)
	 */
	@Override
	protected DmsColor pixelToColor(int pixel) {
		return new DmsColor(pixel);
	}
	
	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#setPixelData(byte[])
	 */
	@Override
	public void setPixelData(byte[] ba) {
		assertValidDmsGraphicByteArray(ba);
		int len = length();
		int pix, r, g, b, i, j;
		i = 0;
		for (j = 0; (j < len); j += 3) {
			// extract BGR
			b = ba[j  ] & 0x0ff;
			g = ba[j+1] & 0x0ff;
			r = ba[j+2] & 0x0ff;
			pix = pixels[i];
			// twist to RGB
			pix = (r << 16) | (g << 8) | b;
			pixels[i] = pix;
			++i;
		}
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#getPixelData()
	 */
	@Override
	public byte[] getPixelData() {
		int len = length();
		int pix, r, g, b, i, j;
		byte[] ba = new byte[len];
		i = 0;
		for (j = 0; (j < len); j += 3) {
			pix = pixels[i];
			// extract RGB
			r = (pix >> 16) & 0x0FF;
			g = (pix >>  8) & 0x0FF;
			b = (pix      ) & 0x0FF;
			// twist to BGR
			ba[j  ] = (byte)b;
			ba[j+1] = (byte)g;
			ba[j+2] = (byte)r;
			++i;
		}
		return ba;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WRaster#classicColorToPixel(int)
	 */
	@Override
	public Integer classicColorToPixel(int cco) {
		ColorClassic cc = ColorClassic.fromOrdinal(cco);
		return (cc != null) ? (cc.clr.rgb() & 0x0ffffff) : null;
	}

	/** Copy a WRaster into this WRaster.
	 * Black pixels in the graphic being copied are
	 * considered transparent.
	 * @param wg WRaster to copy.
	 * @param x0 X-position on raster (0-based).
	 * @param y0 Y-position on raster (0-based). */
	@Override
	public void copy(WRaster wg, int x0, int y0) {
		int w = wg.getWidth();
		int h = wg.getHeight();
		boolean bOutOfBounds = false;
		if (wg.colorscheme == ColorScheme.COLOR_24_BIT) {
			int pix;
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					try {
						pix = wg.getPixel(x, y) & 0x0ffffff;
						if (isLit(pix))
							setPixel(x0 + x, y0 + y, pix);
					}
					catch (IndexOutOfBoundsException ex) {
						bOutOfBounds = true;
						break;
					}
				}
			}
		} else {
			// Any bitmap can be converted to RGB
			DmsColor dms;
			int rgb;
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					try {
						dms = wg.getColor(x, y);
						rgb = dms.rgb() & 0x0ffffff;
						if (isLit(rgb))
							setPixel(x0 + x, y0 + y, rgb);
					}
					catch (IndexOutOfBoundsException ex) {
						bOutOfBounds = true;
						break;
					}
				}
			}
		}
		if (bOutOfBounds)
			throw new IndexOutOfBoundsException();
	}


}
