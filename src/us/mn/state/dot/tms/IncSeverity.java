/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2019  Minnesota Department of Transportation
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
	minor  (near,   INCIDENT_LOW),
	normal (middle, INCIDENT_MED),
	major  (far,    INCIDENT_HIGH);

	/** Maximum range to DMS */
	public final IncRange maximum_range;

	/** Message priority */
	public final DmsMsgPriority priority;

	/** Create an incident severity value */
	private IncSeverity(IncRange r, DmsMsgPriority p) {
		maximum_range = r;
		priority = p;
	}
}
