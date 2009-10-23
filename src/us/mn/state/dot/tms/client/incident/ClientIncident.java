/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.Road;

/**
 * Client-side incident for use before an incident has been logged.
 *
 * @author Douglas Lau
 */
public class ClientIncident implements Incident {

	/** Create a new client incident */
	public ClientIncident(int et, Road rd, short d, int e, int n,
		String i)
	{
		event_type = et;
		road = rd;
		dir = d;
		easting = e;
		northing = n;
		impact = i;
	}

	/** Get the SONAR object name */
	public String getName() {
		return "client_incident";
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Event type */
	protected final int event_type;

	/** Get the event type */
	public int getEventType() {
		return event_type;
	}

	/** Get the event date (timestamp) */
	public long getEventDate() {
		return System.currentTimeMillis();
	}

	/** Road */
	protected final Road road;

	/** Get the road */
	public Road getRoad() {
		return road;
	}

	/** Road direction */
	protected final short dir;

	/** Get the road direction */
	public short getDir() {
		return dir;
	}

	/** UTM easting */
	protected final int easting;

	/** Get the UTM Easting */
	public int getEasting() {
		return easting;
	}

	/** UTM northing */
	protected final int northing;

	/** Get the UTM Northing */
	public int getNorthing() {
		return northing;
	}

	/** Impact string */
	protected String impact;

	/** Get the current impact code */
	public String getImpact() {
		return impact;
	}

	/** Set the impact code */
	public void setImpact(String imp) {
		impact = imp;
	}

	/** Get the cleared status */
	public boolean getCleared() {
		return false;
	}

	/** Set the cleared status */
	public void setCleared(boolean c) {
		// cannot clear an incident which hasn't been logged
	}

	/** Destroy the object */
	public void destroy() {
		// do nothing
	}
}
