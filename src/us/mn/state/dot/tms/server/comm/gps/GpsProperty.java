/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.gps;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.mn.state.dot.tms.server.comm.AsciiDeviceProperty;

/**
 * GPS-modem property (location).
 *
 * @author John L. Stanley
 */
public abstract class GpsProperty
		extends AsciiDeviceProperty {

	/* current GPS-location property
	 *  The lat/lon values are valid after parsing a valid
	 *  response from the GPS device.  You can check to
	 *  see if we received a usable response by calling
	 *  the <property>.gotGpsLock() function.
	 */

	protected double lat = 0.0;
	protected double lon = 0.0;

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	//----------------------------------------------

	/* Did the response say we have a good/bad GPS lock?
	 *  You can get a valid response from the modem -and-
	 *  that response may tell you that it doesn't have
	 *  a good GPS lock.  If this is the case, then the
	 *  gotValidResponse() function will return true,
	 *  but the gotGpsLock() function will return false.
	 */

	protected boolean bGpsLock = false;

	public boolean gotGpsLock() {
		return bGpsLock;
	}

	//----------------------------------------------

	/** Create a new GPS property */
	public GpsProperty(String cmdQueryLocation) {
		super(cmdQueryLocation);
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("lat:");
		sb.append(lat);
		sb.append(" lon:");
		sb.append(lon);
		return sb.toString();
	}
	
	//=====================================================
	// GPS Helper functions
	//=====================================================

	private void clear() {
		lat = 0.0;
		lon = 0.0;
		bGpsLock = false;
	}
	
	//------------------------------
	//---- TAIP Response Parser ----
	//------------------------------
	
    /** Converts TAIP latitude/longitude substrings to a
	 *  decimal-degrees value.
	 * @param s1 = string containing integer part of TAIP number
	 * @param s2 = string containing fractional part of TAIP number
	 * @return Double decimal-degrees value
	 * @throws NumberFormatException if there's a problem.
	 */
	private Double cvtTaip2DecimalDegrees(String s1, String s2) {
		String ssign = "";
		switch (s1.charAt(0)) {
		case '+':  // remove leading '+'
			s1 = s1.substring(1);
			break;
		case '-':  // remove leading '-'
			s1 = s1.substring(1);
			ssign = "-";
		}
		// remove leading '0's (but leave a
		// single zero if that's all there is)
		while ((s1.length() > 1) && (s1.charAt(0) == '0'))
			s1 = s1.substring(1);
		return Double.valueOf(ssign+s1+"."+s2);
	}
	
	//---------------
	
	private final static String patternRPV =
		".*?>RPV\\d{5}(\\d{3})(\\d{5})(\\d{4})(\\d{5})\\.{7}(\\d)[^<]*<";
	private final static Pattern pRPV =	Pattern.compile(patternRPV);
	private final static String patternRCP =
		".*?>RCP\\d{5}(\\d{3})(\\d{4})(\\d{4})(\\d{4})\\.{1}(\\d)[^<]*<";
	private final static Pattern pRCP =	Pattern.compile(patternRCP);
	private final static String patternRLN =
		".*?>RLN\\d{8}(\\d{3})(\\d{7})(\\d{4})(\\d{7})\\.{39}(\\d)[^<]*<";
	private final static Pattern pRLN =	Pattern.compile(patternRLN);

	/** Parses a string containing GPS coordinates in
	 * one of of three TAIP response formats (RCP, RLN, or RPV).
	 * Ignores leading text (if any).
	 * @param sTaipResponse = TAIP response.
	 * @return If the string was successfully parsed,
	 * returns true.  If not successful, returns false.
	 **/
	// FIXME:  Add code to deal with optional TAIP checksum suffix.
	// (Note: Current code ignores TAIP checksum suffix, if present.)
	public boolean parseTaipGps(String sTaipResponse) {
		Matcher m;
		Double xlat, xlon;

		clear();
		m = pRPV.matcher(sTaipResponse);
		if (!m.find()) {
			m = pRCP.matcher(sTaipResponse);
			if (!m.find()) {
				m = pRLN.matcher(sTaipResponse);
				if (!m.find()) {
					return false;
				}
			}
		}

		// Check if AgeOfData indicator says data is not usable.
		if (m.group(5).equals("0"))
			return false;

		try {
			xlat = cvtTaip2DecimalDegrees(m.group(1), m.group(2));
			xlon = cvtTaip2DecimalDegrees(m.group(3), m.group(4));
		}
		catch (NumberFormatException ex) {
			return false;
		}

		lat = xlat;
		lon = xlon;
		bGpsLock = true;
		return true;
	}
	
	//------------------------------
	//---- NMEA Response Parser ----
	//------------------------------

	private final static String patternNmeaCoord =
		"(\\d{0,3})(\\d{2}\\.\\d*)";
	private final static Pattern pNmeaCoord =
		Pattern.compile(patternNmeaCoord);

	/** Parses a single NMEA latitude or longitude coordinate string.
	 * @return Returns the resulting coordinate value.
	 * @throws NumberFormatException if it can't parse the coordinate.
	 */
	private double parseNmeaCoordinate(String sCoord)
			throws NumberFormatException {
		double coord;
		String sDeg;

		// parse the coordinate
		Matcher m = pNmeaCoord.matcher(sCoord);
		if (!m.find())
			throw new NumberFormatException();

		// Either of the following valueOf lines 
		// might throw a NumberFormatException...
		sDeg = m.group(1);
		coord = Double.valueOf(m.group(2)) / 60;

		// sDeg == "" means the degrees portion of
		// the value is zero.
		if (!sDeg.isEmpty())
			coord += Double.valueOf(sDeg);
		
		return coord;
	}
	
	//---------------

	/** Extract lat/lon coordinates from NMEA matcher */
	private void tryExtractNmea(Matcher m, int nLLOffset)
			throws NumberFormatException {
		double xlat, xlon;

		xlat = parseNmeaCoordinate(m.group(nLLOffset));
		if (m.group(nLLOffset+1).equals("S"))
			xlat = -xlat;//   Latitude:  N = positive, S = negative.
	
		xlon = parseNmeaCoordinate(m.group(nLLOffset+2));
		if (m.group(nLLOffset+3).equals("W"))
			xlon = -xlon;//   Longitude: E = positive, W = negative
	
		lat = xlat;
		lon = xlon;	
	}

	//---------------

	/** Checks a response string against a NMEA-sentence pattern.
	 *  If the string matches the pattern, this tries to parse
	 *  the lat/lon values from the string.  If the lat/lon
	 *  values are not there, it places zero in the lat & lon
	 *  class fields.  All GPS-OK status flags in the sentences
	 *  are assumed to be a single character.
	 * @param sNmeaResponse
	 * @param p Pattern to match
	 * @param nLLOffset Offset of the start of the lat/lon fields
	 * @param nStatusOffset Offset to the GPS status field
	 * @param sStatusOK String containing GPS status values considered
	 *  "good".
	 * @return Returns true if the sentence matches the pattern.  It
	 *  does NOT automatically mean that the lat/lon values were provided.
	 */
	private boolean parseNmeaGps(String sNmeaResponse, Pattern p,
			int nLLOffset, 
			int nStatusOffset, String sStatusOK) {
		String stat;

		Matcher m = p.matcher(sNmeaResponse);
		if (!m.find())
			return false;

		try {
			// extract lat/lon
			try {
				tryExtractNmea(m, nLLOffset);
			}
			catch (NumberFormatException e) {
				return true; // we did recognize the NMEA sentence...
			}

			// check GPS-status flag
			stat = m.group(nStatusOffset);
			if (!stat.isEmpty() && (sStatusOK.indexOf(stat.charAt(0)) >= 0))
				bGpsLock = true;
			
			return true;
		}
		catch (IndexOutOfBoundsException e) {
			return false;
		}
	}
				
	//---------------
	
	private final static String patternGPRMC =
		"[^\\$]*\\$GPRMC,([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,\\*]*),?([^\\*]*)?(\\*..)?";
	private final static Pattern pGPRMC = Pattern.compile(patternGPRMC);
	// Example (signal not acquired):
	//	 $GPRMC,235947.000,V,0000.0000,N,00000.0000,E,,,041299,,*1D
	// Example (signal acquired): 
	//	 $GPRMC,092204.999,A,4250.5589,S,14718.5084,E,0.00,89.68,211200,,*25
	// Example (signal acquired):
	//	 $GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W*70
	// Example (signal not acquired, RedLion):
	//	 $GPRMC,011124.00,V,,,,,,,,,,N*7A
	// Example (signal acquired, RedLion):
	//	 $GPRMC,054451.00,A,4101.41021,N,09618.04028,W,002.2,249.7,090716,03.3,E,A*1D
	// GPS Status: A = OK, V = Invalid
	//
	// Note:  The conditional elements near the end of this pattern are
	// there so it will match both 11-field and 12-field responses.  The
	// 12-field version of this sentence is created by a RedLion GPS
	// modem.  (No idea what the extra field contains...)

	private final static String patternGPGGA =
		"[^\\$]*\\$GPGGA,([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,\\*]*),([^\\*]*)?(\\*..)?";
	private final static Pattern pGPGGA = Pattern.compile(patternGPGGA);
	// Example (signal not acquired): $GPGGA,235947.000,0000.0000,N,00000.0000,E,0,00,0.0,0.0,M,,,,0000*00
	// Example (signal acquired): $GPGGA,092204.999,4250.5589,S,14718.5084,E,1,04,24.4,19.7,M,,,,0000*1F
	/* GPS Status:
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

	private final static String patternGPGLL =
		"[^\\$]*\\$GPGLL,([^,]*),([^,]*),([^,]*),([^,]*),([^,\\*]*),([^\\*]*)?(\\*..)?";
	private final static Pattern pGPGLL = Pattern.compile(patternGPGLL);
	// Example (signal not acquired): $GPGLL,0000.0000,N,00000.0000,E,235947.000,V*2D
	// Example (signal acquired): $GPGLL,4250.5589,S,14718.5084,E,092204.999,A*2D
	// GPS Status: A = OK, V = Invalid
	/* Note: There is a older variant of the $GPGLL format
	 *  that does not contain GPS-quality info.  This code
	 *  does not currently recognize that variant.
	 */

	/** Parses a string containing GPS coordinates in any of
	 * 3 "NMEA sentence" formats ($GPRMC, $GPGGA, or $GPGLL).
	 * @param sNmeaResponse = NMEA sentence.
	 * @return Did the string contain a recognizable NMEA response?
	 * This does not automatically mean the lat/lon values were valid.
	 * (NMEA sentences don't always contain the data you want...)
	 * Note:  Current patterns ignore NMEA checksum suffix, if present.)
	 **/
	// FIXME:  Add code to deal with optional NMEA checksum suffix.
	public boolean parseNmeaGps(String sNmeaResponse) {

		clear();
		return parseNmeaGps(sNmeaResponse, pGPRMC, 3, 2, "A")
		    || parseNmeaGps(sNmeaResponse, pGPGGA, 2, 6, "12345678")
		    || parseNmeaGps(sNmeaResponse, pGPGLL, 1, 6, "A");
	}
}
