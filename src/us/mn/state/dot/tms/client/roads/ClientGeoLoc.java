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
package us.mn.state.dot.tms.client.roads;

import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Road;

/**
 * ClientGeoLoc is a client-side GeoLoc implementation.
 *
 * @author Douglas Lau
 */
public class ClientGeoLoc implements GeoLoc {

	/** Create an location */
	public ClientGeoLoc(Road free, short fd, int e, int n) {
		freeway = free;
		free_dir = fd;
		easting = e;
		northing = n;
	}

	/** Get the type name */
	public String getTypeName() {
		return "client_geo_loc";
	}

	/** Get the name */
	public String getName() {
		return "client_geo_loc";
	}

	/** Destroy the loc */
	public void destroy() {
		// nothing to do
	}

	/** Freeway */
	protected final Road freeway;

	/** Set the freeway name */
	public void setFreeway(Road f) {
		// part of GeoLoc interface
	}

	/** Get the freeway name */
	public Road getFreeway() {
		return freeway;
	}

	/** Freeway direction */
	protected final short free_dir;

	/** Set the freeway direction */
	public void setFreeDir(short d) {
		// part of GeoLoc interface
	}

	/** Get the freeway direction */
	public short getFreeDir() {
		return free_dir;
	}

	/** Set the cross-street name */
	public void setCrossStreet(Road x) {
		// part of GeoLoc interface
	}

	/** Get the cross-street name */
	public Road getCrossStreet() {
		return null;
	}

	/** Set the cross street direction */
	public void setCrossDir(short d) {
		// part of GeoLoc interface
	}

	/** Get the cross street direction */
	public short getCrossDir() {
		return 0;
	}

	/** Set the cross street modifier */
	public void setCrossMod(short m) {
		// part of GeoLoc interface
	}

	/** Get the cross street modifier */
	public short getCrossMod() {
		return 0;
	}

	/** UTM Easting */
	protected final int easting;

	/** Set the UTM Easting */
	public void setEasting(Integer x) {
		// part of GeoLoc interface
	}

	/** Get the UTM Easting */
	public Integer getEasting() {
		return easting;
	}

	/** Set the UTM Easting offset */
	public void setEastOffset(Integer x) {
		// part of GeoLoc interface
	}

	/** Get the UTM Easting offset */
	public Integer getEastOffset() {
		return null;
	}

	/** UTM Northing */
	protected final int northing;

	/** Set the UTM Northing */
	public void setNorthing(Integer y) {
		// part of GeoLoc interface
	}

	/** Get the UTM Northing */
	public Integer getNorthing() {
		return northing;
	}

	/** Set the UTM Northing offset */
	public void setNorthOffset(Integer y) {
		// part of GeoLoc interface
	}

	/** Get the UTM Northing offset */
	public Integer getNorthOffset() {
		return null;
	}
}
