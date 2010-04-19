/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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
 * An enumeration of all r_node transitions.  The ordinal values correspond to
 * the records in the iris.r_node_transition look-up table.
 *
 * @author Douglas Lau
 */
public enum R_NodeTransition {

	/** None transition (0) */
	NONE("None"),

	/** Loop transition (1) */
	LOOP("Loop"),

	/** Leg transition (2) */
	LEG("Leg"),

	/** Slipramp transition (3) */
	SLIPRAMP("Slipramp"),

	/** CD road transition (4) */
	CD("CD"),

	/** HOV transition (5) */
	HOV("HOV"),

	/** Common section transition (6) */
	COMMON("Common"),

	/** Flyover transition (7) */
	FLYOVER("Flyover");

	/** Create a new r_node transition */
	private R_NodeTransition(String d) {
		description = d;
	}

	/** R_Node transition description */
	public final String description;

	/** Get an r_node transition from an ordinal value */
	static public R_NodeTransition fromOrdinal(int o) {
		for(R_NodeTransition rt: R_NodeTransition.values()) {
			if(rt.ordinal() == o)
				return rt;
		}
		return null;
	}

	/** Get an array of r_node transition descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(R_NodeTransition rt: R_NodeTransition.values())
			d.add(rt.description);
		return d.toArray(new String[0]);
	}
}
