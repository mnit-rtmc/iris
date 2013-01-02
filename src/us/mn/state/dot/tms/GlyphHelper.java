/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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

import java.util.Iterator;

/**
 * Helper class for glyphs.
 *
 * @author Douglas Lau
 */
public class GlyphHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private GlyphHelper() {
		assert false;
	}

	/** Lookup the glyph with the specified name */
	static public Glyph lookup(String name) {
		return (Glyph)namespace.lookupObject(Glyph.SONAR_TYPE, name);
	}

	/** Get a glyph iterator */
	static public Iterator<Glyph> iterator() {
		return new IteratorWrapper<Glyph>(namespace.iterator(
			Glyph.SONAR_TYPE));
	}
}
