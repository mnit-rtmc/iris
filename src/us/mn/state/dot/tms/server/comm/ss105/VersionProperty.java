/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss105;

import java.io.IOException;

/**
 * Firmware Version Property.  This is not a documented message, so it is not
 * supported by HD sensors.
 *
 * @author Douglas Lau
 */
public class VersionProperty extends SS105Property {

	/** Firmware version */
	protected String version;

	/** Check if the request has a checksum */
	protected boolean hasChecksum() {
		return false;
	}

	/** Format a basic "GET" request */
	protected String formatGetRequest() {
		return "S5";
	}

	/** Parse the response to a QUERY */
	protected void parseQuery(String r) {
		version = r;
	}

	/** Get the sensor firmware version */
	public String getVersion() {
		return version;
	}

	/** Get a string representation */
	public String toString() {
		return "Version " + version;
	}
}
