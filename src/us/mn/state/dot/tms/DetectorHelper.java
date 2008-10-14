/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
 * Helper class for detectors.
 *
 * @author Douglas Lau
 */
public class DetectorHelper {

	/** Future detector label */
	static protected final String FUTURE = "FUTURE";

	/** Don't allow instances to be created */
	private DetectorHelper() {
		assert false;
	}

	/** Get the root label (for a detector or a station) */
	static public String getRootLabel(Detector det) {
		GeoLoc loc = getGeoLoc(det);
		if(loc == null)
			return FUTURE;
		Road freeway = loc.getFreeway();
		Road cross = loc.getCrossStreet();
		if(freeway == null || cross == null)
			return FUTURE;
		short fd = loc.getFreeDir();
		short cd = loc.getCrossDir();
		short cm = loc.getCrossMod();
		StringBuilder b = new StringBuilder();
		b.append(freeway.getAbbrev());
		b.append("/");
		if(cd > 0)
			b.append(TMSObject.DIRECTION[cd]);
		b.append(TMSObject.MOD_SHORT[cm]);
		b.append(cross.getAbbrev());
		b.append(TMSObject.DIR_FREEWAY[fd]);
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
		Integer an = getNumber(a);
		Integer bn = getNumber(b);
		if(an != null && bn != null)
			return an.compareTo(bn);
		else
			return a.getName().compareTo(b.getName());
	}

	/** Get the detector number */
	static protected Integer getNumber(Detector det) {
		try {
			return Integer.valueOf(det.getName());
		}
		catch(NumberFormatException e) {
			return null;
		}
	}
}
