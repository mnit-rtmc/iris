/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.util.Comparator;
import java.util.TreeSet;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.Road;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * A detector set is a logical grouping of detectors
 *
 * @author Douglas Lau
 */
public class DetectorSet {

	/** Estimated sustainable capacity of mainline right lanes */
	static private final int CAP_RIGHT_LANE = 1800;

	/** Estimated sustainable capacity of other lanes */
	static private final int CAP_OTHER_LANE = 2100;

	/** Threshold for a drop in density to indicate an incident */
	static private final int DENSITY_DROP_THRESHOLD = 50;

	/** Density considered "full" for space capacity calculation */
	static private final int FULL_DENSITY = 32;

	/** Detector comparator */
	static private final Comparator<DetectorImpl> COMPARATOR =
		new Comparator<DetectorImpl>()
	{
		public int compare(DetectorImpl a, DetectorImpl b) {
			int la = a.getLaneNumber();
			int lb = b.getLaneNumber();
			int n = la - lb;
			if (n == 0) {
				return NumericAlphaComparator.compareStrings(
					a.getName(), b.getName());
			} else
				return n;
		}
	};

	/** Set of detectors */
	private final TreeSet<DetectorImpl> detectors =
		new TreeSet<DetectorImpl>(COMPARATOR);

	/** Add a detector to the detector set */
	public void addDetector(DetectorImpl det) {
		detectors.add(det);
	}

	/** Remove a detector from the detector set */
	public void removeDetector(DetectorImpl det) {
		detectors.remove(det);
	}

	/** Remove all detectors from the detector set */
	public void clear() {
		detectors.clear();
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
		if (other == null)
			return;
		for (DetectorImpl det: other.detectors)
			detectors.add(det);
	}

	/** Add the detectors of the given type from another detector set */
	public void addDetectors(DetectorSet other, LaneType lt) {
		for (DetectorImpl d: other.detectors) {
			if (lt.ordinal() == d.getLaneType())
				addDetector(d);
		}
	}

	/** Remove all the detectors from another detector set */
	public boolean removeDetectors(DetectorSet other) {
		boolean match = false;
		for (DetectorImpl det: other.detectors) {
			if (detectors.remove(det))
				match = true;
		}
		return match;
	}

	/** Test if the detector set is defined */
	public boolean isDefined() {
		for (DetectorImpl det: detectors) {
			if (det.isActive())
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
		for (DetectorImpl det: detectors) {
			if (det.getFlow() == MISSING_DATA)
				return false;
		}
		return true;
	}

	/** Test if the detector set is (defined and) sampling "real" data */
	public boolean isPerfect() {
		for (DetectorImpl det: detectors) {
			if (!det.isSampling())
				return false;
		}
		return isDefined();
	}

	/** Get the current volume of the detector set */
	public int getVolume() {
		boolean defined = false;
		int vol = 0;
		for (DetectorImpl det: detectors) {
			int v = det.getVolume();
			if (v < 0)
				return v;
			vol += v;
			defined = true;
		}
		return defined ? vol : MISSING_DATA;
	}

	/** Get the current flow rate of the detector set.
		Note: assumes that isGood returned true. */
	public int getFlow() {
		int flow = 0;
		for (DetectorImpl det: detectors)
			flow += (int)det.getFlow();
		return flow;
	}

	/** Get the average density of the detector set */
	public float getDensity() {
		float k = 0;
		int n_dets = 0;
		for (DetectorImpl det: detectors) {
			float d = det.getDensity();
			if (d >= 0) {
				k += d;
				n_dets++;
			}
		}
		return (n_dets > 0) ? (k / n_dets) : MISSING_DATA;
	}

	/** Get the max density */
	public Double getMaxDensity() {
		Double k = null;
		for (DetectorImpl det: detectors) {
			double d = det.getDensity();
			if (d >= 0 && (k == null || d > k))
				k = d;
		}
		return k;
	}

	/** Get the maximum occupancy for the detector set */
	public float getMaxOccupancy() {
		float occ = 0;
		for (DetectorImpl det: detectors)
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
		for (DetectorImpl det: detectors) {
			float d = det.getDensity();
			float s = det.getSpeed();
			if (d > max_density && s != MISSING_DATA) {
				max_density = d;
				speed = s;
			}
		}
		if (max_density < 0)
			return MISSING_DATA;
		float spare_density = FULL_DENSITY - max_density;
		if (spare_density <= 0)
			return 0;
		else
			return spare_density * speed;
	}

	/** Check if (mainline detector set) is flowing */
	public boolean isFlowing() {
		float d_this = 0;
		float d_last = 0;
		Road xStreet = null;
		for (DetectorImpl det: detectors) {
			float d = det.getDensity();
			if (d < 0)
				continue;
			GeoLoc loc = det.lookupGeoLoc();
			if (xStreet != null &&
				xStreet == loc.getCrossStreet())
			{
				d_this = Math.max(d_this, d);
			} else {
				d_last = d_this;
				d_this = d;
				xStreet = loc.getCrossStreet();
			}
			if (d_this < d_last - DENSITY_DROP_THRESHOLD)
				return false;
		}
		return true;
	}

	/** Get a string representation (for debugging) */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('\'');
		for (DetectorImpl det: detectors) {
			sb.append(det.getName());
			sb.append(' ');
		}
		if (detectors.size() == 0)
			sb.append('\'');
		else
			sb.setCharAt(sb.length() - 1, '\'');
		return sb.toString();
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
