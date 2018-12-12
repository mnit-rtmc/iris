/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.redlion;

/**
 * GPS data (location).
 *
 * @author Douglas Lau
 */
public class GpsData {

	/** Was the GPS signal locked (valid) */
	public final boolean lock;

	/** Latitude of GPS location */
	public final double lat;

	/** Longitude of GPS location */
	public final double lon;

	/** Create a new GPS property */
	public GpsData(boolean lk, double lt, double ln) {
		lock = lk;
		lat = lt;
		lon = ln;
	}
}
