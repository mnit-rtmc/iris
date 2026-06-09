/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026  Minnesota Department of Transportation
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
 * Alarm helper
 *
 * @author Douglas Lau
 */
public class AlarmHelper extends BaseHelper {

	/** Disallow instantiation */
	private AlarmHelper() {
		assert false;
	}

	/** Get an iterator */
	static public Iterator<Alarm> iterator() {
		return new IteratorWrapper<Alarm>(
			namespace.iterator(Alarm.SONAR_TYPE));
	}

	/** Lookup the alarm with the specified name */
	static public Alarm lookup(String name) {
		return (Alarm) namespace.lookupObject(Alarm.SONAR_TYPE, name);
	}
}
