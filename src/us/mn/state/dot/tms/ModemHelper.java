/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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
 * Helper for modems.
 *
 * @author Douglas Lau
 */
public class ModemHelper extends BaseHelper {

	/** Disallow instantiation */
	private ModemHelper() {
		assert false;
	}

	/** Find a modem using a Checker */
	static public Modem find(final Checker<Modem> checker) {
		return (Modem)namespace.findObject(Modem.SONAR_TYPE, checker);
	}
}
