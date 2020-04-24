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
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.GlyphHelper;

/** WFont caches glyphs for characters in a font.
 * 
 * @author John L. Stanley - SRF Consulting
 */
//TODO:  Test:  Would JIT loading of glyphs be faster?
public class WFont implements Font {

	/** referenced Font */
	Font font;
	
	/** ArrayList (indexed by charNum) of glyphs */
	private ArrayList<WGlyph> glyphList =
			new ArrayList<WGlyph>();
	
	/** Maximum char width in font */
	private int maxCharWidth;

	/** Pre-expand an ArrayList and pad with nulls. */
	private void ensureListSize(
			ArrayList<WGlyph> la,
			int maxIndex) {
		while (la.size() < maxIndex+1)
			la.add(null);
	}

	/** Construct a WFont.
	 * (Also pre-loads all WGlyphs in the font.) */
	public WFont(Font f) {
		font = f;

		// preload all glyphs for font
		Iterator<Glyph> itg = GlyphHelper.iterator();
		Glyph g;
		WGlyph wg;
		int charNum;
		int charWidth;
		maxCharWidth = 0;
		while (itg.hasNext()) {
			g = itg.next();
			if ((g == null) || (g.getFont() != f))
				continue;
			try {
				wg = new WGlyph(g);
				charNum = wg.getCharNum();
				ensureListSize(glyphList, charNum);
				glyphList.set(charNum, wg);
				charWidth = wg.width;
				if (maxCharWidth < charWidth)
					maxCharWidth = charWidth;
			}
			catch (NullPointerException ex) {
				ex.printStackTrace();
			}
			catch (IndexOutOfBoundsException ex) {
				ex.printStackTrace();
			}
		}
	}

	/** Get glyph for int charNum */
	public WGlyph getGlyph(int charNum) {
		try {
			return glyphList.get(charNum);
		}
		catch (NullPointerException ex) {
			return null;
		}
		catch (IndexOutOfBoundsException ex) {
			return null;
		}
	}
	
	/** Get glyph for char ch */
	public WGlyph getGlyph(char ch) {
		return getGlyph((int)ch);
	}
	
	/** Returns an Iterator<WGlyph> for all WGlyphs
	 *  in the font ordered by character number */
	public Iterator<WGlyph> glyphs(int fontNum) {
		return glyphList.iterator();
	}
	
	//===========================================
	// Tools for figuring out pixel-width of
	// various kinds of text.
	
	/** Get maximum character width in font */
	public int getMaxCharWidth() {
		return maxCharWidth;
	}

	/** Get maximum pixel-width of a field that
	 *  is len characters wide. (The number
	 *  returned for variable-width fonts may
	 *  seem large because it uses the widest
	 *  char in the font to do the math.) */
	public int getFieldWidth(int len) {
		int csep = getCharSpacing();
		int width = (maxCharWidth * len) + (csep * (len - 1));
		return Math.max(0,  width);
	}

	/** Get pixel-width of a specified string.
	 *  Any chars in the string that are not
	 *  in the font are ignored. */
	public int getTextWidth(String str) {
		int len = str.length();
		if (len < 1)
			return 0;
		char ch;
		WGlyph glyph;
		int csep = getCharSpacing();
		int width = 0;
		for (int i = 0; i < len; i++){
		    ch = str.charAt(i);
		    glyph = getGlyph(ch);
		    if (glyph != null)
		    	width += glyph.getWidth() + csep;
		}
		return Math.max(0, (width - csep));
	}

	/** Get pixel-width of a specified integer.
	 *  (converted to a string) */
	public int getIntWidth(int i) {
		String str = Integer.toString(i);
		return getTextWidth(str);
	}
	
	/** Get max pixel-width required to display
	 *  any number in a range of numbers */
	public int getIntWidth(int imin, int imax) {
		int wid = 0;
		int w2;
		for (int i = imin; (i <= imax); ++i) {
			w2 = getIntWidth(i);
			if (wid < w2)
				wid = w2;
		}
		return wid;
	}
	
	//===========================================
	// Pass-thru methods for accessing the underlying Font
	
	/* (non-Javadoc)
	 * @see us.mn.state.dot.sonar.SonarObject#destroy()
	 */
	@Override
	public void destroy() {
		font.destroy();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.sonar.SonarObject#getName()
	 */
	public String getName() {
		return font.getName();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.sonar.SonarObject#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return font.getTypeName();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Font#setNumber(int)
	 */
	@Override
	public void setNumber(int n) {
		font.setNumber(n);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Font#getNumber()
	 */
	@Override
	public int getNumber() {
		return font.getNumber();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Font#setHeight(int)
	 */
	@Override
	public void setHeight(int h) {
		font.setHeight(h);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Font#getHeight()
	 */
	@Override
	public int getHeight() {
		return font.getHeight();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Font#setWidth(int)
	 */
	@Override
	public void setWidth(int w) {
		font.setWidth(w);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Font#getWidth()
	 */
	@Override
	public int getWidth() {
		return font.getWidth();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Font#setLineSpacing(int)
	 */
	@Override
	public void setLineSpacing(int s) {
		font.setLineSpacing(s);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Font#getLineSpacing()
	 */
	@Override
	public int getLineSpacing() {
		return font.getLineSpacing();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Font#setCharSpacing(int)
	 */
	@Override
	public void setCharSpacing(int s) {
		font.setCharSpacing(s);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Font#getCharSpacing()
	 */
	@Override
	public int getCharSpacing() {
		return font.getCharSpacing();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Font#setVersionID(int)
	 */
	@Override
	public void setVersionID(int v) {
		font.setVersionID(v);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.Font#getVersionID()
	 */
	@Override
	public int getVersionID() {
		return font.getVersionID();
	}

}
