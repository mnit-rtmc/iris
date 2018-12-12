/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
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

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.AsciiDeviceProperty;

/**
 * GPS property for querying a device's location
 * using the RedLion "AT+BGPSGT" command.
 *
 * @author John L. Stanley
 * @author Douglas Lau
 */
public class RedLionProperty extends AsciiDeviceProperty {

	/** Create a new GPS property for RedLion protocol */
	public RedLionProperty() {
		// RedLion modem: Query the GPS reporting data
		super("AT+BGPSGT\r");
		max_chars = 200;
	}

	/** GPS data from response */
	private GpsData gps_data;

	/** Did we get a GPS lock (valid)? */
	public boolean gotGpsLock() {
		return (gps_data != null) ? gps_data.lock : false;
	}

	/** Get GPS latitude */
	public double getLat() {
		return (gps_data != null) ? gps_data.lat : 0;
	}

	/** Get GPS longitude */
	public double getLon() {
		return (gps_data != null) ? gps_data.lon : 0;
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
		gps_data = TaipResponse.parse(resp);
		if (gps_data != null)
			return true;
		gps_data = NmeaResponse.parse(resp);
		return (gps_data != null);
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "lat:" + getLat() + " lon:" + getLon();
	}
}
