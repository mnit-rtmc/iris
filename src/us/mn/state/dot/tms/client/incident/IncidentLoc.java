/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
 * Copyright (C) 2014  AHMCT, University of California
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

import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.Road;

/**
 * IncidentLoc is a client-side GeoLoc implementation for incidents.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class IncidentLoc implements GeoLoc {

	/** Incident in question */
	protected final Incident incident;

	/** Location of nearest r_node on corridor */
	protected final GeoLoc rnd_loc;

	/** Create an incident location */
	public IncidentLoc(Incident loc) {
		this(loc, null);
	}

	/** Create an incident location */
	public IncidentLoc(Incident inc, GeoLoc rlc) {
		incident = inc;
		rnd_loc = rlc;
	}

	/** Get the type name */
	public String getTypeName() {
		return "incident_loc";
	}

	/** Get the name */
	public String getName() {
		return "iloc_" + incident.getName();
	}

	/** Destroy the incident loc */
	public void destroy() {
		// nothing to do
	}

	/** Set the roadway name */
	public void setRoadway(Road f) {
		// part of GeoLoc interface
	}

	/** Get the roadway name */
	public Road getRoadway() {
		return incident.getRoad();
	}

	/** Set the roadway direction */
	public void setRoadDir(short d) {
		// part of GeoLoc interface
	}

	/** Get the roadway direction */
	public short getRoadDir() {
		return incident.getDir();
	}

	/** Set the cross-street name */
	public void setCrossStreet(Road x) {
		// part of GeoLoc interface
	}

	/** Get the cross-street name */
	public Road getCrossStreet() {
		if(rnd_loc != null)
			return rnd_loc.getCrossStreet();
		else
			return null;
	}

	/** Set the cross street direction */
	public void setCrossDir(short d) {
		// part of GeoLoc interface
	}

	/** Get the cross street direction */
	public short getCrossDir() {
		if(rnd_loc != null)
			return rnd_loc.getCrossDir();
		else
			return 0;
	}

	/** Set the cross street modifier */
	public void setCrossMod(short m) {
		// part of GeoLoc interface
	}

	/** Get the cross street modifier */
	public short getCrossMod() {
		if(rnd_loc != null)
			return rnd_loc.getCrossMod();
		else
			return 0;
	}

	/** Set the latitude */
	public void setLat(Double lt) {
		// part of GeoLoc interface
	}

	/** Get the latitude */
	public Double getLat() {
		return incident.getLat();
	}

	/** Set the longitude */
	public void setLon(Double ln) {
		// part of GeoLoc interface
	}

	/** Get the longitude */
	public Double getLon() {
		return incident.getLon();
	}

	/** Set the milepoint (not applicable). */
	public void setMilepoint(String m) {
		// part of GeoLoc interface
	}

	/**
	 * Get the milepoint (not applicable).
	 * @return null
	 */
	public String getMilepoint() {
		// part of GeoLoc interface
		return null;
	}

	/** Get a description of an incident location */
	public String getDescription() {
		switch(LaneType.fromOrdinal(incident.getLaneType())) {
		case MERGE:
			return GeoLocHelper.getOnRampDescription(rnd_loc);
		case EXIT:
			return GeoLocHelper.getOffRampDescription(rnd_loc);
		default:
			return GeoLocHelper.getDescription(rnd_loc);
		}
	}
}
