/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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

import java.util.LinkedList;

/**
 * Controller condition enumeration.   The ordinal values correspond to the
 * records in the iris.condition look-up table.
 *
 * @author Douglas Lau
 */
public enum CtrlCondition {
	PLANNED,	// 0
	ACTIVE,		// 1
	CONSTRUCTION,	// 2
	REMOVED;	// 3

	/** Get a controller condition from an ordinal value */
	static public CtrlCondition fromOrdinal(int o) {
		for (CtrlCondition c: values()) {
			if (c.ordinal() == o)
				return c;
		}
		return PLANNED;
	}

	/** Get an array of condition descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for (CtrlCondition c: values())
			d.add(c.toString());
		return d.toArray(new String[0]);
	}

	/** Get values with null as first */
	static public CtrlCondition[] values_with_null() {
		return new CtrlCondition[] {
			null,
			PLANNED,
			ACTIVE,
			CONSTRUCTION,
			REMOVED,
		};
	}
}
