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
		while (itg.hasNext()) {
			g = itg.next();
			if ((g == null) || (g.getFont() != f))
				continue;
			try {
				wg = new WGlyph(g);
				charNum = wg.getCharNum();
				ensureListSize(glyphList, charNum);
				glyphList.set(charNum, wg);
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
