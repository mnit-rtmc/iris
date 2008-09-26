/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import java.util.TreeSet;

/**
 * A detector set is a logical grouping of detectors
 *
 * @author Douglas Lau
 */
public class DetectorSet implements Constants {

	/** Estimated sustainable capacity of mainline right lanes */
	static protected final int CAP_RIGHT_LANE = 1800;

	/** Estimated sustainable capacity of other lanes */
	static protected final int CAP_OTHER_LANE = 2100;

	/** Threshold for a drop in density to indicate an incident */
	static protected final int DENSITY_DROP_THRESHOLD = 50;

	/** Density considered "full" for space capacity calculation */
	static protected final int FULL_DENSITY = 32;

	/** Set of detectors */
	protected final TreeSet<DetectorImpl> detectors =
		new TreeSet<DetectorImpl>();

	/** Add a detector to the detector set */
	public void addDetector(DetectorImpl det) {
		detectors.add(det);
	}

	/** Get the number of detectors in the detector set */
	public int size() {
		return detectors.size();
	}

	/** Check if another detector set is the same */
	public boolean isSame(DetectorSet other) {
		return detectors.equals(other.detectors);
	}

	/** Add all the detectors from another detector set */
	public void addDetectors(DetectorSet other) {
		if(other == null)
			return;
		for(DetectorImpl det: other.detectors)
			detectors.add(det);
	}

	/** Add the detectors of the given type from another detector set */
	public void addDetectors(DetectorSet other, LaneType lt) {
		for(DetectorImpl d: other.detectors) {
			if(lt.ordinal() == d.getLaneType())
				addDetector(d);
		}
	}

	/** Remove all the detectors from another detector set */
	public boolean removeDetectors(DetectorSet other) {
		boolean match = false;
		for(DetectorImpl det: other.detectors) {
			if(detectors.remove(det))
				match = true;
		}
		return match;
	}

	/** Test if the detector set is defined */
	public boolean isDefined() {
		for(DetectorImpl det: detectors) {
			if(det.isActive())
				return true;
		}
		return false;
	}

	/** Test if the detector set is (defined and) good */
	public boolean isGood() {
		return isDefined() && isNotBad();
	}

	/** Test if the detector set is not bad */
	public boolean isNotBad() {
		for(DetectorImpl det: detectors) {
			if(det.getFlow() == MISSING_DATA)
				return false;
		}
		return true;
	}

	/** Test if the detector set is (defined and) sampling "real" data */
	public boolean isPerfect() {
		for(DetectorImpl det: detectors) {
			if(det.isFailed() || !det.isSampling())
				return false;
		}
		return isDefined();
	}

	/** Get the current flow rate of the detector set.
		Note: assumes that isGood returned true. */
	public int getFlow() {
		int flow = 0;
		for(DetectorImpl det: detectors)
			flow += (int)det.getFlow();
		return flow;
	}

	/** Get the maximum occupancy for the detector set */
	public float getMaxOccupancy() {
		float occ = 0;
		for(DetectorImpl det: detectors)
			occ = Math.max(det.getOccupancy(), occ);
		return occ;
	}

	/** Get the "sustainable" capacity (mainline station) */
	public int getCapacity() {
		return CAP_RIGHT_LANE + CAP_OTHER_LANE * (detectors.size() - 1);
	}

	/** Calculate the upstream (one lane) capacity for a mainline zone */
	public float getUpstreamCapacity() {
		float max_density = MISSING_DATA;
		float speed = MISSING_DATA;
		for(DetectorImpl det: detectors) {
			float d = det.getDensity();
			float s = det.getSpeed();
			if(d > max_density && s != MISSING_DATA) {
				max_density = d;
				speed = s;
			}
		}
		if(max_density < 0)
			return MISSING_DATA;
		float spare_density = FULL_DENSITY - max_density;
		if(spare_density <= 0)
			return 0;
		else
			return spare_density * speed;
	}

	/** Check if (mainline detector set) is flowing */
	public boolean isFlowing() {
		float d_this = 0;
		float d_last = 0;
		Road xStreet = null;
		for(DetectorImpl det: detectors) {
			float d = det.getDensity();
			if(d < 0)
				continue;
			GeoLoc loc = det.lookupGeoLoc();
			if(xStreet != null &&
				xStreet == loc.getCrossStreet())
			{
				d_this = Math.max(d_this, d);
			} else {
				d_last = d_this;
				d_this = d;
				xStreet = loc.getCrossStreet();
			}
			if(d_this < d_last - DENSITY_DROP_THRESHOLD)
				return false;
		}
		return true;
	}

	/** Get a string representation (for debugging) */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append('\'');
		for(DetectorImpl det: detectors) {
			buf.append(det.getIndex());
			buf.append(' ');
		}
		if(detectors.size() == 0)
			buf.append('\'');
		else
			buf.setCharAt(buf.length() - 1, '\'');
		return buf.toString();
	}

	/** Get the detector set for the given lane type */
	public DetectorSet getDetectorSet(LaneType lt) {
		DetectorSet set = new DetectorSet();
		set.addDetectors(this, lt);
		return set;
	}

	/** Convert a detector set to an array of detectors */
	public DetectorImpl[] toArray() {
		return detectors.toArray(new DetectorImpl[0]);
	}
}
