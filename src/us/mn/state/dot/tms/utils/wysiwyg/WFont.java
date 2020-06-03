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
public class WFont implements Font {

	/** referenced Font */
	Font font;
	
	/** ArrayList (indexed by charNum) of glyphs */
	private ArrayList<WGlyph> glyphList =
			new ArrayList<WGlyph>();
	
	/** Maximum char width in font */
	private int maxCharWidth;
	
	//===========================================
	
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
	
	/** Returns an Iterator&lt;WGlyph&gt; for all WGlyphs
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

	/** Get character spacing
	 * @param chsp Character spacing (null = use font default)
	 */
	public int getCharSpacing(Integer chsp) {
		if (chsp != null)
			return chsp;
		return getCharSpacing();
	}

	/** Get pixel-width of a specified string.
	 *  Any chars in the string that are not
	 *  in the font are ignored.
	 * @param chsp Character spacing (null = use font default)
	 * @param str  string to measure
	 */
	public int getTextWidth(Integer chsp, String str) {
		int len = str.length();
		if (len < 1)
			return 0;
		char ch;
		WGlyph glyph;
		int charSpacing = getCharSpacing(chsp);
		int width = 0;
		for (int i = 0; i < len; i++){
		    ch = str.charAt(i);
		    glyph = getGlyph(ch);
		    if (glyph != null)
		    	width += glyph.getWidth() + charSpacing;
		}
		return Math.max(0, (width - charSpacing));
	}

	/** Get pixel-width of a specified char.
	 *  Any chars that are not in the font return 0.
	 * @param ch Character number
	 */
	public int getCharWidth(char ch) {
		WGlyph glyph = getGlyph(ch);
		return (glyph == null) ? 0 : glyph.getWidth();
	}

	/** Get pixel-width of a specified integer.
	 *  (converted to a string).
	 * @param chsp Character spacing (null = use font default)
	 * @param i    Number to measure.
	 */
	public int getIntWidth(Integer chsp, int i) {
		String str = Integer.toString(i);
		return getTextWidth(chsp, str);
	}
	
	/** Get max pixel-width required to display
	 *  any number in a range of numbers.
	 * @param chsp Character spacing (null = use font default)
	 * @param imin Minimum number to measure
	 * @param imax Maximum number to measure
	 */
	public int getIntWidth(Integer chsp, int imin, int imax) {
		int wid = 0;
		int w2;
		for (int i = imin; (i <= imax); ++i) {
			w2 = getIntWidth(chsp, i);
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
