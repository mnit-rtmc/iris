/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
 * A tag reader is a sensor for vehicle transponders, which are used for
 * toll lanes.
 *
 * @author Douglas Lau
 */
public interface TagReader extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "tag_reader";

	/** Get the device location */
	GeoLoc getGeoLoc();
}
