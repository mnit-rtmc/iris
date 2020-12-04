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

import java.util.ArrayList;
import java.util.Iterator;

import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;

/** Cache of WFont(s) for use by WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 */
public class WFontCache {
	
	/** Array (indexed by fontNum) of Font objects */
	private Font[] fontArray;
	
	/** Array (indexed by fontNum) of WFont objects.
	 * WFonts are created, loaded with glyphs, and added
	 * to this array as needed by getFont(int fontNum) */
	private WFont[] wfontArray;
	
	/** Maximum font number */
	private int maxFont = -1;

	/**
	 * Constructor
	 */
	public WFontCache() {
		int fontNum;
		Font font;
		// figure out maxFontNum
		Iterator<Font> itf = FontHelper.iterator();
		while (itf.hasNext()) {
			fontNum = itf.next().getNumber();
			if (maxFont < fontNum)
				maxFont = fontNum;
		}
		// construct font arrays
		fontArray  = new Font[maxFont+1];
		wfontArray = new WFont[maxFont+1];
		// preload all Font(s)
		itf = FontHelper.iterator();
		while (itf.hasNext()) {
			font = itf.next();
			fontNum = font.getNumber();
			if ((fontNum >= 0) && (fontNum <= maxFont)) {
				fontArray[fontNum] = font;
			}
		}
	}

	/** Returns the maximum font number.
	 * Returns -1 if there are no fonts.
	 */
	public int getMaxFontNum() {
		return maxFont;
	}

	/** Returns a Font for the given font number.
	 *  Returns null if there is no such font.
	 *  (Faster than FontHelper.find(...).)
	 *  
	 * @param fontNum
	 * @return Matching Font or null
	 */
	public Font getBasicFont(int fontNum) {
		try {
			return fontArray[fontNum];
		}
		catch (IndexOutOfBoundsException ex) {
			return null;
		}
	}

	/** Returns a WFont for the given font number.
	 *  Returns null if there is no such font.
	 *  
	 * @param fontNum
	 * @return Matching WFont or null
	 */
	public WFont getWFont(int fontNum) {
		try {
			WFont wf = wfontArray[fontNum];
			if (wf != null)
				return wf;
			Font f = getBasicFont(fontNum);
			if (f == null)
				return null;  // font not in DB
			wf = new WFont(f);
			wfontArray[fontNum] = wf;
			return wf;
		}
		catch (IndexOutOfBoundsException ex) {
			return null;
		}
	}

	/** Returns an Iterator<Font> ordered by font number. */
	Iterator<Font> fontsByNum() {
		ArrayList<Font> fal = new ArrayList<Font>();
		Font font;
		for (int i = 0; (i < fontArray.length); ++i) {
			font = fontArray[i];
			if (font != null)
				fal.add(font);
		}
		return fal.iterator();
	}
	
	/** Returns a WGlyph for the given font and
	 *  char numbers.  Returns null if there is
	 *  no such glyph.
	 *  
	 *  Faster than GlyphHelper.lookup(String) after
	 *  the first reference to a given font.
	 *  
	 * @param fontNum
	 * @param charNum
	 * @return Matching WGlyph or null
	 */
	public WGlyph getWGlyph(int fontNum, int charNum) {
		WFont wf = getWFont(fontNum);
		if (wf == null)
			return null;
		return wf.getGlyph(charNum);
	}

	/** Returns a WGlyph for the given font and
	 *  character.  Returns null if there is
	 *  no such glyph.
	 */
	public WGlyph getWGlyph(int fontNum, char ch) {
		return getWGlyph(fontNum, (int)ch);
	}
}

