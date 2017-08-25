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
package us.mn.state.dot.tms;

/**
 * TransGeoLoc is a transient GeoLoc implementation.
 *
 * @author Douglas Lau
 */
public class TransGeoLoc implements GeoLoc {

	/** Create a transient location.
	 * @param road Road location.
	 * @param rd Road direction.
	 * @param lt Latitude.
	 * @param ln Longitude. */
	public TransGeoLoc(Road road, short rd, float lt, float ln) {
		roadway = road;
		road_dir = rd;
		lat = lt;
		lon = ln;
		distance = 0;
	}

	/** Create a transient location that includes the
	 * distance from the roadway to the lat/lon point.
	 * @param road Road location.
	 * @param rd Road direction.
	 * @param lt Latitude.
	 * @param ln Longitude.
	 * @param d Distance. */
	public TransGeoLoc(Road road, short rd, float lt, float ln, double d) {
		roadway = road;
		road_dir = rd;
		lat = lt;
		lon = ln;
		distance = d;
	}

	/** Get the type name */
	@Override
	public String getTypeName() {
		return "trans_geo_loc";
	}

	/** Get the name */
	@Override
	public String getName() {
		return "trans_geo_loc";
	}

	/** Check group membership */
	@Override
	public boolean isInGroup(String g) {
		return false;
	}

	/** Destroy the loc */
	@Override
	public void destroy() {
		// nothing to do
	}

	/** Roadway */
	private final Road roadway;

	/** Set the roadway name */
	@Override
	public void setRoadway(Road f) { }

	/** Get the roadway name */
	@Override
	public Road getRoadway() {
		return roadway;
	}

	/** Roadway direction */
	private final short road_dir;

	/** Set the roadway direction */
	@Override
	public void setRoadDir(short d) { }

	/** Get the roadway direction */
	@Override
	public short getRoadDir() {
		return road_dir;
	}

	/** Set the cross-street name */
	@Override
	public void setCrossStreet(Road x) { }

	/** Get the cross-street name */
	@Override
	public Road getCrossStreet() {
		return null;
	}

	/** Set the cross street direction */
	@Override
	public void setCrossDir(short d) { }

	/** Get the cross street direction */
	@Override
	public short getCrossDir() {
		return (short) Direction.UNKNOWN.ordinal();
	}

	/** Set the cross street modifier */
	@Override
	public void setCrossMod(short m) { }

	/** Get the cross street modifier */
	@Override
	public short getCrossMod() {
		return (short) LocModifier.AT.ordinal();
	}

	/** Latitude */
	private final double lat;

	/** Set the latitude */
	@Override
	public void setLat(Double lt) { }

	/** Get the latitude */
	@Override
	public Double getLat() {
		return lat;
	}

	/** Longitude */
	private final double lon;

	/** Set the longitude */
	@Override
	public void setLon(Double ln) { }

	/** Get the longitude */
	@Override
	public Double getLon() {
		return lon;
	}

	/** Set the landmark */
	@Override
	public void setLandmark(String lm) { }

	/** Get the landmark */
	@Override
	public String getLandmark() {
		return null;
	}

	/** Distance from selected point (in spherical mercator "meters") */
	private final double distance;

	/** Get distance from selected point (in spherical mercator "meters") */
	public double getDistance() {
		return distance;
	}
}
