/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * A detector set is a logical grouping of detectors, ordered by lane number.
 *
 * @author Douglas Lau
 */
public class DetectorSet {

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

	/** Create an empty detector set */
	public DetectorSet() { }

	/** Get all detectors */
	public ArrayList<DetectorImpl> getAll() {
		return new ArrayList<DetectorImpl>(detectors);
	}

	/** Add a detector to the detector set */
	public void addDetector(DetectorImpl det) {
		detectors.add(det);
	}

	/** Remove a detector from the detector set */
	public void removeDetector(DetectorImpl det) {
		detectors.remove(det);
	}

	/** Convert a detector set to an array of detectors */
	public DetectorImpl[] toArray() {
		return detectors.toArray(new DetectorImpl[0]);
	}
}
