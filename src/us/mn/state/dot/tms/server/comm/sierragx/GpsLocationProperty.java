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
package us.mn.state.dot.tms.server.comm.sierragx;

import java.io.IOException;

/**
 * Property to send a "AT*GPSDATA?" command and to parse a
 * response from a Sierra Wireless GX-series modem containing
 * GPS location information.
 *
 * @author John L. Stanley
 */
public class GpsLocationProperty extends SierraGxProperty {

	private int hitCount = 0;

	private boolean bGpsLock;
	private double lat;
	private double lon;

	/** Create a new GPS location property */
	public GpsLocationProperty() {
		super("AT*GPSDATA?\r");
	}

	/** Parse an integer off the end of a string
	 * @param str String to check
	 * @param prefix Prefix we're looking for
	 * @return If it finds the prefix, it returns an Integer
	 * containing the numeric value after the prefix.
	 * If it doesn't find the prefix, or there's no
	 * number after the prefix it returns null.
	 */
	protected Integer parseInteger(String prefix, String str) {
		try {
			if (!str.startsWith(prefix))
				return null;
			String suf = str.substring(prefix.length());
			return new Integer(suf);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	/** Parse a double off the end of a string
	 * @param str String to check
	 * @param prefix Prefix we're looking for
	 * @return If it finds the prefix, it returns a Double
	 * containing the numeric value after the prefix.
	 * If it doesn't find the prefix, or there's no
	 * number after the prefix it returns null.
	 */
	protected Double parseDouble(String prefix, String str) {
		try {
			if (!str.startsWith(prefix))
				return null;
			String suf = str.substring(prefix.length());
			return new Double(suf);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	/** Parse three lines from SierraWireless GX modem's four line
	 *  response.  (This ignores the "Satellite Count=" response
	 *  line.)
	 * @param in InputStream from device
	 * @return Returns false until we have parsed a full response.
	 * @throws IOException if a response line is longer than max_chars.
	 */
	@Override
	protected boolean parseResponse(String resp) throws IOException {
		Integer iVal;
		Double dVal;

		iVal = parseInteger("GPS Fix=", resp);
		if (iVal != null) {
			bGpsLock = (iVal != 0);
			hitCount = 1;
			return false; // keep looking
		}
		dVal = parseDouble("Latitude=", resp);
		if (dVal != null) {
			lat = dVal;
			++hitCount;
			return false; // keep looking
		}
		dVal = parseDouble("Longitude=", resp);
		if (dVal != null) {
			lon = dVal;
			return (++hitCount == 3);
		}
		return false; // keep looking
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "lat:" + lat + " lon:" + lon;
	}
}
