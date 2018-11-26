/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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

import java.util.Iterator;

/**
 * Helper class for detectors.
 *
 * @author Douglas Lau
 */
public class DetectorHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private DetectorHelper() {
		assert false;
	}

	/** Lookup the detector with the specified name */
	static public Detector lookup(String name) {
		return (Detector)namespace.lookupObject(Detector.SONAR_TYPE,
			name);
	}

	/** Get a detector iterator */
	static public Iterator<Detector> iterator() {
		return new IteratorWrapper<Detector>(namespace.iterator(
			Detector.SONAR_TYPE));
	}

	/** Get the geo location of a detector */
	static public GeoLoc getGeoLoc(Detector d) {
		R_Node n = d.getR_Node();
		return (n != null) ? n.getGeoLoc() : null;
	}

	/** Get the detector label */
	static public String getLabel(Detector det) {
		if (det.getLaneType() == LaneType.PARKING.ordinal())
			return getParkingLabel(det);
		StringBuilder b = new StringBuilder();
		b.append(GeoLocHelper.getRootLabel(getGeoLoc(det)));
		if (b.toString().equals(GeoLocHelper.FUTURE))
			return b.toString();
		LaneType lt = LaneType.fromOrdinal(det.getLaneType());
		b.append(lt.suffix);
		int l_num = det.getLaneNumber();
		if (l_num > 0)
			b.append(l_num);
		if (det.getAbandoned())
			b.append("-ABND");
		return b.toString();
	}

	/** Get detector label for a parking detector */
	static private String getParkingLabel(Detector det) {
		StringBuilder b = new StringBuilder();
		b.append(GeoLocHelper.getParkingRoot(getGeoLoc(det)));
		int l_num = det.getLaneNumber();
		if (l_num <= PARKING_LANE.length)
			b.append(PARKING_LANE[l_num]);
		if (det.getAbandoned())
			b.append("-ABND");
		return b.toString();
	}

	/** Parking lane codes (head-front, head-rear, tail-front, tail-rear) */
	static private final String PARKING_LANE[] = {
		"", "hf", "hr", "tf", "tr"
	};

	/** Test if a detector is active */
	static public boolean isActive(Detector d) {
		return ControllerHelper.isActive(d.getController())
		    && !d.getAbandoned();
	}
}
