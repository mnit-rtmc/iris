/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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

	/** Set the freeway name */
	public void setFreeway(Road f) {
		// part of GeoLoc interface
	}

	/** Get the freeway name */
	public Road getFreeway() {
		return incident.getRoad();
	}

	/** Set the freeway direction */
	public void setFreeDir(short d) {
		// part of GeoLoc interface
	}

	/** Get the freeway direction */
	public short getFreeDir() {
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

	/** Set the UTM Easting */
	public void setEasting(Integer x) {
		// part of GeoLoc interface
	}

	/** Get the UTM Easting */
	public Integer getEasting() {
		return incident.getEasting();
	}

	/** Set the UTM Easting offset */
	public void setEastOffset(Integer x) {
		// part of GeoLoc interface
	}

	/** Get the UTM Easting offset */
	public Integer getEastOffset() {
		return null;
	}

	/** Set the UTM Northing */
	public void setNorthing(Integer y) {
		// part of GeoLoc interface
	}

	/** Get the UTM Northing */
	public Integer getNorthing() {
		return incident.getNorthing();
	}

	/** Set the UTM Northing offset */
	public void setNorthOffset(Integer y) {
		// part of GeoLoc interface
	}

	/** Get the UTM Northing offset */
	public Integer getNorthOffset() {
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
