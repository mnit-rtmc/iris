/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import us.mn.state.dot.sonar.Checker;

/**
 * Helper class for detectors.
 *
 * @author Douglas Lau
 */
public class DetectorHelper extends BaseHelper {

	/** Future detector label */
	static protected final String FUTURE = "FUTURE";

	/** Pattern to match detector names */
	static protected final Pattern DET_NAME =
		Pattern.compile("([0-9]*)(.*)");

	/** Don't allow instances to be created */
	private DetectorHelper() {
		assert false;
	}

	/** Get the root label (for a detector or a station) */
	static public String getRootLabel(Detector det) {
		GeoLoc loc = getGeoLoc(det);
		if(loc == null)
			return FUTURE;
		Road roadway = loc.getRoadway();
		Road cross = loc.getCrossStreet();
		if(roadway == null || cross == null)
			return FUTURE;
		Direction rd = Direction.fromOrdinal(loc.getRoadDir());
		Direction cd = Direction.fromOrdinal(loc.getCrossDir());
		LocModifier cm = LocModifier.fromOrdinal(loc.getCrossMod());
		StringBuilder b = new StringBuilder();
		b.append(roadway.getAbbrev());
		b.append("/");
		b.append(cd.abbrev);
		b.append(cm.abbrev);
		b.append(cross.getAbbrev());
		b.append(rd.det_dir);
		return b.toString();
	}

	/** Get the geo location of a detector */
	static public GeoLoc getGeoLoc(Detector det) {
		R_Node n = det.getR_Node();
		if(n != null)
			return n.getGeoLoc();
		else
			return null;
	}

	/** Get the detector label */
	static public String getLabel(Detector det) {
		StringBuilder b = new StringBuilder();
		b.append(getRootLabel(det));
		if(b.toString().equals(FUTURE))
			return FUTURE;
		LaneType lt = LaneType.fromOrdinal(det.getLaneType());
		b.append(lt.suffix);
		int l_num = det.getLaneNumber();
		if(l_num > 0)
			b.append(l_num);
		if(det.getAbandoned())
			b.append("-ABND");
		return b.toString();
	}

	/** Get a station label */
	static public String getStationLabel(Detector det) {
		return getRootLabel(det);
	}

	/** Compare two detectors for sorting */
	static public int compare(Detector a, Detector b) {
		String an = parseName(a.getName());
		String bn = parseName(b.getName());
		return an.compareTo(bn);
	}

	/** Parse a detector name */
	static protected String parseName(String n) {
		Matcher m = DET_NAME.matcher(n);
		if(m.find())
			return formatInteger(m.group(1)) + m.group(2);
		else
			return n;
	}

	/** Format an integer to eight digits */
	static protected String formatInteger(String s) {
		Integer n = parseInteger(s);
		if(n != null) {
			StringBuilder b = new StringBuilder();
			b.append(n.toString());
			while(b.length() < 8)
				b.insert(0, '0');
			return b.toString();
		} else
			return "";
	}

	/** Parse a string and return an integer */
	static protected Integer parseInteger(String s) {
		try {
			return Integer.valueOf(s);
		}
		catch(NumberFormatException e) {
			return null;
		}
	}

	/** Find detectors using a Checker */
	static public Detector find(Checker<Detector> checker) {
		return (Detector)namespace.findObject(Detector.SONAR_TYPE,
			checker);
	}

	/** Lookup the detector with the specified name */
	static public Detector lookup(String name) {
		return (Detector)namespace.lookupObject(Detector.SONAR_TYPE,
			name);
	}
}
