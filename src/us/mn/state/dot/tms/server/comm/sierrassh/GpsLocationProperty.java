/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2024  SRF Consulting Group
 * Copyright (C) 2018-2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.sierrassh;

import java.io.IOException;

import us.mn.state.dot.tms.server.comm.AsciiDeviceProperty;

/**
 * Property to send a "AT*GPSDATA?" command and to parse a
 * response from a Sierra Wireless RV-series modem containing
 * GPS location information.
 *
 * @author John L. Stanley
 * @author Douglas Lau
 */
public class GpsLocationProperty extends AsciiDeviceProperty {

	/** Parse an integer off the end of a string.
	 * @param str String to check.
	 * @param prefix Prefix we're looking for.
	 * @return If it finds the prefix, it returns an Integer containing the
	 *         numeric value after the prefix.  If it doesn't find the
	 *         prefix or there's no number after the prefix it returns null.
	 */
	static protected Integer parseInteger(String prefix, String str) {
		try {
			if (!str.startsWith(prefix))
				return null;
			String suf = str.substring(prefix.length());
			return Integer.valueOf(suf);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	/** Parse a double off the end of a string.
	 * @param str String to check.
	 * @param prefix Prefix we're looking for.
	 * @return If it finds the prefix, it returns a Double containing the
	 *         numeric value after the prefix.  If it doesn't find the
	 *         prefix or there's no number after the prefix it returns null.
	 */
	static private Double parseDouble(String prefix, String str) {
		try {
			if (!str.startsWith(prefix))
				return null;
			String suf = str.substring(prefix.length());
			return Double.valueOf(suf);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	/** GPS lock flag */
	private Boolean gps_lock;

	/** Did we get a GPS lock (valid)? */
	public boolean gotGpsLock() {
		return (gps_lock != null) ? gps_lock : false;
	}

	/** GPS latitude */
	private Double lat;

	/** Get GPS latitude */
	public double getLat() {
		return (lat != null) ? lat : 0;
	}
	
	/** GPS longitude */
	private Double lon;

	/** Get GPS longitude */
	public double getLon() {
		return (lon != null) ? lon : 0;
	}

	/** Create a new SierraWireless RV modem GPS location property */
	public GpsLocationProperty() {
		super("AT*GPSDATA?\r");
	}

	/** Parse three lines from SierraWireless RV modem's four line
	 *  response.  (This ignores the "Satellite Count=" response
	 *  line.)
	 * @param resp Response string.
	 * @return Returns false until we have parsed a full response.
	 * @throws IOException if a response line is longer than max_chars.
	 */
	@Override
	protected boolean parseResponse(String resp) throws IOException {
		System.out.println("> "+resp);
		Integer lk = parseInteger("GNSS Fix=", resp);
		if (lk == null)
			lk = parseInteger("GPS Fix=", resp);
		if (lk != null)
			gps_lock = (lk != 0);
		Double lt = parseDouble("Latitude=", resp);
		if (lt != null)
			lat = lt;
		Double ln = parseDouble("Longitude=", resp);
		if (ln != null)
			lon = ln;
		return (gps_lock != null) && (lat != null) && (lon != null);
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "lat:" + lat + " lon:" + lon;
	}
}
