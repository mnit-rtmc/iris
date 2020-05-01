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

import java.io.IOException;

import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.Glyph;

/** Specialized version of WRasterMono1
 *  used for holding/manipulating
 *  a bitmap for a MULTI character.
 *  
 *  This takes the place of the old
 *  Glyph and BitmapGraphic classes
 *  for WYSIWYG rendering.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WGlyph extends WRasterMono1 {

	private Font font;
	private int  fontNum;
	private int  charNum;

	/** Get the font */
	public Font getFont() {
		return font;
	};

	/** Get the font number */
	public int getFontNum() {
		return fontNum;
	};

	/** Get the character number (aka "code point") */
	public int getCharNum() {
		return charNum;
	}

	/**
	 * Construct a WGlyph from a Glyph
	 */
	public WGlyph(Glyph g) {
		super(g.getWidth(), g.getFont().getHeight());
		this.font    = g.getFont();
		this.fontNum = font.getNumber();
		this.charNum = g.getCodePoint();
		try {
			this.setEncodedPixels(g.getPixels());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

