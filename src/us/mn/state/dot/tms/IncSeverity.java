/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2023  Minnesota Department of Transportation
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

import static us.mn.state.dot.tms.IncRange.*;

/**
 * Incident severity enumeration.
 *
 * @author Douglas Lau
 */
public enum IncSeverity {
	minor  (near,   SignMsgPriority.high_2),
	normal (middle, SignMsgPriority.high_3),
	major  (far,    SignMsgPriority.high_4);

	/** Maximum range to DMS */
	public final IncRange maximum_range;

	/** Message priority */
	public final SignMsgPriority priority;

	/** Create an incident severity value */
	private IncSeverity(IncRange r, SignMsgPriority p) {
		maximum_range = r;
		priority = p;
	}
}
