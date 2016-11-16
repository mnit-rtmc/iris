/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Font helper methods.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class FontHelper extends BaseHelper {

	/** Fixed default font number */
	static public final int DEFAULT_FONT_NUM = 1;

	/** Disallow instantiation */
	private FontHelper() {
		assert false;
	}

	/** Lookup a Font in the SONAR namespace. 
	 * @return The specified font or null if it does not exist. */
	static public Font lookup(String name) {
		return (Font)namespace.lookupObject(Font.SONAR_TYPE, name);
	}

	/** Get a font iterator */
	static public Iterator<Font> iterator() {
		return new IteratorWrapper<Font>(namespace.iterator(
			Font.SONAR_TYPE));
	}

	/** Find a font using a font number */
	static public Font find(int f_num) {
		Iterator<Font> it = iterator();
		while(it.hasNext()) {
			Font f = it.next();
			if(f.getNumber() == f_num)
				return f;
		}
		return null;
	}

	/** Fint the lowest unused font number */
	static public int findUnusedFontNumber() {
		HashSet<Integer> numbers = new HashSet<Integer>();
		Iterator<Font> it = iterator();
		while(it.hasNext()) {
			Font f = it.next();
			numbers.add(f.getNumber());
		}
		for(int i = 1; i < 256; i++) {
			if(!numbers.contains(i))
				return i;
		}
		// This can only happen if we already have 255 fonts defined
		return 0;
	}

	/** Lookup the glyphs in the specified font */
	static public Collection<Glyph> lookupGlyphs(Font font) {
		TreeMap<Integer, Glyph> glyphs = new TreeMap<Integer, Glyph>();
		Iterator<Glyph> it = GlyphHelper.iterator();
		while(it.hasNext()) {
			Glyph g = it.next();
			if(g.getFont() == font)
				glyphs.put(g.getCodePoint(), g);
		}
		return glyphs.values();
	}

	/** Lookup a glyph in the specified font */
	static public Glyph lookupGlyph(Font font, int cp) {
		Iterator<Glyph> it = GlyphHelper.iterator();
		while(it.hasNext()) {
			Glyph g = it.next();
			if(g.getFont() == font && g.getCodePoint() == cp)
				return g;
		}
		return null;
	}

	/** Look up a code point in the specified font */
	static public Graphic lookupGraphic(Font font, int cp)
		throws InvalidMsgException
	{
		Glyph g = lookupGlyph(font, cp);
		if(g != null) {
			Graphic gr = g.getGraphic();
			if(gr != null)
				return gr;
		}
		throw new InvalidMsgException("code point: " + cp);
	}

	/** Calculate the width of a span of text.
	 * @param font Font to use for text
	 * @param t Text to calculate
	 * @return Width in pixels of text
	 * @throws InvalidMsgException if the font is missing a character */
	static public int calculateWidth(Font font, String t)
		throws InvalidMsgException
	{
		return calculateWidth(font, t, font.getCharSpacing());
	}

	/** Calculate the width of a span of text.
	 * @param font Font to use for text.
	 * @param t Text to calculate.
	 * @param cs Character spacing.
	 * @return Width in pixels of text.
	 * @throws InvalidMsgException if the font is missing a character */
	static public int calculateWidth(Font font, String t, int cs)
		throws InvalidMsgException
	{
		int w = 0;
		for(int i = 0; i < t.length(); i++) {
			if(i > 0)
				w += cs;
			int cp = t.charAt(i);
			Graphic c = lookupGraphic(font, cp);
			w += c.getWidth();
		}
		return w;
	}
}
