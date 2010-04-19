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
 * An enumeration of all r_node types.  The ordinal values correspond to the
 * records in the iris.r_node_type look-up table.
 *
 * @author Douglas Lau
 */
public enum R_NodeType {

	/** Station r_node type (0) */
	STATION("Station"),

	/** Entrance r_node type (1) */
	ENTRANCE("Entrance"),

	/** Exit r_node type (2) */
	EXIT("Exit"),

	/** Intersection r_node type (3) */
	INTERSECTION("Intersection"),

	/** Access r_node type (4) */
	ACCESS("Access"),

	/** Interchange r_node type (5) */
	INTERCHANGE("Interchange");

	/** Create a new r_node type */
	private R_NodeType(String d) {
		description = d;
	}

	/** R_Node type description */
	public final String description;

	/** Get an r_node type from an ordinal value */
	static public R_NodeType fromOrdinal(int o) {
		for(R_NodeType rt: R_NodeType.values()) {
			if(rt.ordinal() == o)
				return rt;
		}
		return null;
	}

	/** Get an array of r_node type descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(R_NodeType rt: R_NodeType.values())
			d.add(rt.description);
		return d.toArray(new String[0]);
	}
}
