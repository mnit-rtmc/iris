/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.gps.redlion;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.gps.GpsProperty;

/**
 * GPS property for querying a device's location
 * using the RedLion "AT+BGPSGT" command.
 *
 * @author John L. Stanley
 */
public class GpsRedLionProperty extends GpsProperty {

	/** Create a new GPS property for RedLion protocol */
	public GpsRedLionProperty() {
		// RedLion modem: Query the GPS reporting data
		super("AT+BGPSGT\r");
		
		max_chars = 200;
	}

	/** Parses TAIP -and- NMEA responses:
	 * RedLion modems may be in TAIP or NMEA mode.  We
	 * try to parse responses in either protocol and
	 * report lat/long info from the first line containing
	 * a string that looks like a response in any of
	 * the six (3 from each protocol) supported formats.
	 */
	@Override
	protected boolean parseResponse(String resp) throws IOException {
		return parseTaipGps(resp)
		    || parseNmeaGps(resp);
	}
	
}
