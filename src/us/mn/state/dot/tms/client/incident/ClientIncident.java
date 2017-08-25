/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentDetail;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.geo.Position;

/**
 * Client-side incident for use before an incident has been logged.
 *
 * @author Douglas Lau
 */
public class ClientIncident implements Incident {

	/** Name for all client incidents */
	static public final String NAME = "client_incident";

	/** Create a new client incident */
	public ClientIncident(String rpl, int et, IncidentDetail id, short lnt,
		Road rd, short d, double lt, double ln, String i)
	{
		replaces = rpl;
		event_type = et;
		detail = id;
		lane_type = LaneType.fromOrdinal(lnt);
		road = rd;
		dir = d;
		lat = lt;
		lon = ln;
		impact = i;
	}

	/** Get the SONAR object name */
	@Override
	public String getName() {
		// must be unique due to selection model
		return NAME + Integer.toString(System.identityHashCode(this));
	}

	/** Check group membership */
	@Override
	public boolean isInGroup(String g) {
		return false;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Name of replaced incident */
	private final String replaces;

	/** Get name of incident this replaces.  Note: for client incidents,
	 * this is not the original incident (in a chain), but the previous
	 * incident. */
	@Override
	public String getReplaces() {
		return replaces;
	}

	/** Event type */
	private final int event_type;

	/** Get the event type */
	@Override
	public int getEventType() {
		return event_type;
	}

	/** Get the event date (timestamp) */
	@Override
	public long getEventDate() {
		return System.currentTimeMillis();
	}

	/** Incident detail */
	private final IncidentDetail detail;

	/** Get the incident detail */
	@Override
	public IncidentDetail getDetail() {
		return detail;
	}

	/** Lane type */
	private final LaneType lane_type;

	/** Get the lane type ordinal */
	@Override
	public short getLaneType() {
		return (short) lane_type.ordinal();
	}

	/** Road */
	private final Road road;

	/** Get the road */
	@Override
	public Road getRoad() {
		return road;
	}

	/** Road direction */
	private final short dir;

	/** Get the road direction */
	@Override
	public short getDir() {
		return dir;
	}

	/** Latitude */
	private final double lat;

	/** Get the latitude */
	@Override
	public double getLat() {
		return lat;
	}

	/** Longitude */
	private final double lon;

	/** Get the longitude */
	@Override
	public double getLon() {
		return lon;
	}

	/** Get the verification camera */
	@Override
	public Camera getCamera() {
		return null;
	}

	/** Impact string */
	private String impact;

	/** Get the current impact code */
	@Override
	public String getImpact() {
		return impact;
	}

	/** Set the impact code */
	@Override
	public void setImpact(String imp) {
		impact = imp;
	}

	/** Get the cleared status */
	@Override
	public boolean getCleared() {
		return false;
	}

	/** Set the cleared status */
	@Override
	public void setCleared(boolean c) {
		// cannot clear an incident which hasn't been logged
	}

	/** Get the confirmed status */
	@Override
	public boolean getConfirmed() {
		return false;
	}

	/** Destroy the object */
	@Override
	public void destroy() {
		// do nothing
	}

	/** Get Position in WGS84 */
	public Position getWgs84Position() {
		return new Position(lat, lon);
	}
}
