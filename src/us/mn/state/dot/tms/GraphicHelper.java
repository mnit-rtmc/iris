/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.Checker;

/**
 * Graphic helper methods.
 *
 * @author Douglas Lau
 */
public class GraphicHelper extends BaseHelper {

	/** Disallow instantiation */
	protected GraphicHelper() {
		assert false;
	}

	/** Find the graphic using a Checker */
	static public Graphic find(final Checker<Graphic> checker) {
		return (Graphic)namespace.findObject(Graphic.SONAR_TYPE, 
			checker);
	}

	/** Find a graphic using a graphic number */
	static public Graphic find(final int g_num) {
		return find(new Checker<Graphic>() {
			public boolean check(Graphic g) {
				Integer gn = g.getGNumber();
				return gn != null && gn == g_num;
			}
		});
	}

	/** Lookup the graphic with the specified name */
	static public Graphic lookup(String name) {
		return (Graphic)namespace.lookupObject(Graphic.SONAR_TYPE,name);
	}
}
