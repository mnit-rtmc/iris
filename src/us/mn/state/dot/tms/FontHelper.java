/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import java.util.Collection;
import java.util.TreeMap;
import us.mn.state.dot.sonar.Checker;

/**
 * Font helper methods.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class FontHelper extends BaseHelper {

	/** Disallow instantiation */
	protected FontHelper() {
		assert false;
	}

	/** Find the font using a font number */
	static public Font find(final int f_num) {
		return (Font)namespace.findObject(Font.SONAR_TYPE, 
			new Checker<Font>()
		{
			public boolean check(Font f) {
				return f.getNumber() == f_num;
			}
		});
	}

	/** Lookup a Font in the SONAR namespace. 
	 * @return The specified font or null if it does not exist. */
	static public Font lookup(String name) {
		return (Font)namespace.lookupObject(Font.SONAR_TYPE, name);
	}

	/** Lookup the glyphs in the specified font */
	static public Collection<Glyph> lookupGlyphs(final Font font) {
		final TreeMap<Integer, Glyph> glyphs =
			new TreeMap<Integer, Glyph>();
		namespace.findObject(Glyph.SONAR_TYPE, new Checker<Glyph>() {
			public boolean check(Glyph g) {
				if(g.getFont() == font)
					glyphs.put(g.getCodePoint(), g);
				return false;
			}
		});
		return glyphs.values();
	}
}
