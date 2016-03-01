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
package us.mn.state.dot.tms.server.comm.incfeed;

import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.IncidentDetail;
import us.mn.state.dot.tms.IncidentDetailHelper;

/**
 * Parsed incident.
 *
 * @author Douglas Lau
 */
public class ParsedIncident {

	/** Parse an incident type */
	static private EventType parseType(String t) {
		switch (t) {
		case "CRASH":
			return EventType.INCIDENT_CRASH;
		case "STALL":
			return EventType.INCIDENT_STALL;
		case "HAZARD":
			return EventType.INCIDENT_HAZARD;
		case "ROADWORK":
			return EventType.INCIDENT_ROADWORK;
		default:
			return null;
		}
	}

	/** Parse an incident detail */
	static private IncidentDetail parseDetail(String d) {
		return IncidentDetailHelper.lookup(d);
	}

	/** Parse a double value */
	static private Double parseDouble(String d) {
		try {
			return Double.parseDouble(d);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Unparsed incident line */
	private final String line;

	/** Incident ID */
	public final String id;

	/** Incident type */
	public final EventType inc_type;

	/** Incident detail */
	public final IncidentDetail detail;

	/** Latitude */
	public final Double lat;

	/** Longitude */
	public final Double lon;

	/** Create a new parsed incident */
	public ParsedIncident(String line) {
		this.line = line;
		String[] inc = line.split(",", 5);
		id = (inc.length > 0) ? inc[0] : null;
		inc_type = (inc.length > 1) ? parseType(inc[1]) : null;
		detail = (inc.length > 2) ? parseDetail(inc[2]) : null;
		lat = (inc.length > 3) ? parseDouble(inc[3]) : null;
		lon = (inc.length > 4) ? parseDouble(inc[4]) : null;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return isValid() ? parsedToString() : "ERR: " + line;
	}

	/** Get parsed string representation */
	private String parsedToString() {
		return id + "," + inc_type + "," + detail + "," + lat +
		       "," + lon;
	}

	/** Check if incident is valid */
	public boolean isValid() {
		return (inc_type != null) && (lat != null) && (lon != null);
	}
}
