/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NMEA response parser.
 *
 * @author John L. Stanley
 * @author Douglas Lau
 */
public class NmeaResponse {

	/** Pattern to match NMEA coordinate value */
	static private final Pattern COORD = Pattern.compile(
		"(\\d{0,3})(\\d{2}\\.\\d*)");

	/** Pattern to match a GPRMC sentence.
	 *
	 * Example (signal not acquired):
	 *     $GPRMC,235947.000,V,0000.0000,N,00000.0000,E,,,041299,,*1D
	 * Example (signal acquired):
	 *     $GPRMC,092204.999,A,4250.5589,S,14718.5084,E,0.00,89.68,211200,,*25
	 * Example (signal acquired):
	 *     $GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W*70
	 * Example (signal not acquired, RedLion):
	 *     $GPRMC,011124.00,V,,,,,,,,,,N*7A
	 * Example (signal acquired, RedLion):
	 *     $GPRMC,054451.00,A,4101.41021,N,09618.04028,W,002.2,249.7,090716,03.3,E,A*1D
	 *
	 * GPS Status: A = OK, V = Invalid
	 *
	 * Note: The conditional elements near the end of this pattern are
	 *       there so it will match both 11-field and 12-field responses.
	 *       The 12-field version of this sentence is created by a RedLion
	 *       GPS modem.  (No idea what the extra field contains...) */
	static private final Pattern GPRMC = Pattern.compile(
	    "[^\\$]*\\$GPRMC,([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,\\*]*),?([^\\*]*)?(\\*..)?");

	/** Pattern to match a GPGGA sentence.
	 *
	 * Example (signal not acquired):
	 *     $GPGGA,235947.000,0000.0000,N,00000.0000,E,0,00,0.0,0.0,M,,,,0000*00
	 * Example (signal acquired):
	 *     $GPGGA,092204.999,4250.5589,S,14718.5084,E,1,04,24.4,19.7,M,,,,0000*1F
	 *
	 * GPS Status:
	 *    0 = invalid,
	 *    1 = GPS fix (SPS),
	 *    2 = DGPS fix,
	 *    3 = PPS fix,
	 *    4 = Real Time Kinematic,
	 *    5 = Float RTK,
	 *    6 = estimated (dead reckoning) (2.3 feature),
	 *    7 = Manual input mode,
	 *    8 = Simulation mode
	 */
	static private final Pattern GPGGA = Pattern.compile(
	    "[^\\$]*\\$GPGGA,([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,\\*]*),([^\\*]*)?(\\*..)?");

	/** Pattern to match a GPGLL sentence.
	 *
	 * Example (signal not acquired):
	 *     $GPGLL,0000.0000,N,00000.0000,E,235947.000,V*2D
	 * Example (signal acquired):
	 *     $GPGLL,4250.5589,S,14718.5084,E,092204.999,A*2D
	 *
	 * GPS Status: A = OK, V = Invalid
	 *
	 * Note: There is a older variant of the $GPGLL format that does not
	 *       contain GPS-quality info.  This code does not currently
	 *       recognize that variant. */
	static private final Pattern GPGLL = Pattern.compile(
		"[^\\$]*\\$GPGLL,([^,]*),([^,]*),([^,]*),([^,]*),([^,\\*]*),([^\\*]*)?(\\*..)?");

	/** Parse a single latitude or longitude coordinate string.
	 * @return Parsed coordinate value.
	 * @throws NumberFormatException if parsing fails. */
	static private double parseCoord(String c) {
		Matcher m = COORD.matcher(c);
		if (!m.find())
			throw new NumberFormatException();
		double coord = Double.valueOf(m.group(2)) / 60;
		String deg = m.group(1);
		// Empty "" means the degrees portion of the value is zero
		if (!deg.isEmpty())
			coord += Double.valueOf(deg);
		return coord;
	}

	/** Try to extract lat/lon coordinates from a matcher.
	 * @param lock Flag indicating GPS lock.
	 * @param m Matcher to parse.
	 * @param offset Group offset of lat/lon fields.
	 * @throws NumberFormatException if parsing fails. */
	static private GpsData tryExtract(boolean lock, Matcher m, int offset) {
		double lat = parseCoord(m.group(offset));
		if (m.group(offset + 1).equals("S"))
			lat = -lat; // Latitude:  N = positive, S = negative.
		double lon = parseCoord(m.group(offset + 2));
		if (m.group(offset + 3).equals("W"))
			lon = -lon; // Longitude: E = positive, W = negative
		return new GpsData(lock, lat, lon);
	}

	/** Parse a response string against a NMEA-sentence pattern.
	 * @param r Response to parse.
	 * @param p Pattern to match.
	 * @param offset Group offset of the lat/lon fields.
	 * @param stat_off Offset to the GPS status field
	 * @param stat_good String containing GPS status values considered
	 *                  "good".  All status flags are assumed to be a single
	 *                  character.
	 * @return GPS data object, or null if sentence doesn't match.
	 *         If the sentence matches, but lat/lon values can't be parsed,
	 *         0,0 is returned. */
	static private GpsData parse(String r, Pattern p, int offset,
		int stat_off, String stat_good)
	{
		Matcher m = p.matcher(r);
		if (!m.find())
			return null;
		try {
			String stat = m.group(stat_off);
			boolean lock = (!stat.isEmpty())
			            && (stat_good.indexOf(stat.charAt(0)) >= 0);
			try {
				return tryExtract(lock, m, offset);
			}
			catch (NumberFormatException e) {
				// we did recognize the NMEA sentence...
				return new GpsData(false, 0, 0);
			}
		}
		catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/** Parse a string containing GPS coordinates in any of 3 "NMEA
	 * sentence" formats ($GPRMC, $GPGGA, or $GPGLL).
	 * This does not automatically mean the lat/lon values were valid.
	 * (NMEA sentences don't always contain the data you want...)
	 * Note: Current patterns ignore NMEA checksum suffix, if present.)
	 *
	 * @param r NMEA response.
	 * @return GPS data object, or null if unable to parse. */
	static public GpsData parse(String r) {
		// FIXME: Add code to deal with optional NMEA checksum suffix.
		GpsData gps_data = parse(r, GPRMC, 3, 2, "A");
		if (null == gps_data)
			gps_data = parse(r, GPGGA, 2, 6, "12345678");
		if (null == gps_data)
			gps_data = parse(r, GPGLL, 1, 6, "A");
		return gps_data;
	}

	/** Disallow instantiation */
	private NmeaResponse() { }
}
