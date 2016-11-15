/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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

import static us.mn.state.dot.tms.DmsMsgPriority.*;
import static us.mn.state.dot.tms.IncRange.*;

/**
 * Incident severity enumeration.
 *
 * @author Douglas Lau
 */
public enum IncSeverity {
	minor  (near,   false, INCIDENT_LOW),
	normal (middle, true,  INCIDENT_MED),
	major  (far,    true,  INCIDENT_HIGH);

	/** Maximum range to DMS */
	public final IncRange range;

	/** Branching flag */
	public final boolean branching;

	/** Message priority */
	public final DmsMsgPriority priority;

	/** Create an incident severity value */
	private IncSeverity(IncRange r, boolean b, DmsMsgPriority p) {
		range = r;
		branching = b;
		priority = p;
	}

	/** Get a severify from an ordinal value */
	static public IncSeverity fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return null;
	}
}
