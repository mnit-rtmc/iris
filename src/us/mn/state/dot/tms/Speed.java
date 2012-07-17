/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  AHMCT, University of California
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

import us.mn.state.dot.tms.utils.SString;

/**
 * Immutable speed, which can also be in a missing state.
 *
 * @author Michael Darter
 */
final public class Speed extends PhysicalQuantity {

	/** Speed in KPH */
	private final double speed_kph;

	/** Constructor for missing or non-missing value.
	 * @param l Speed in KPH or null for missing. */
	public Speed(Integer l) {
		super(l == null);
		speed_kph = (l == null ? 0 : l);
	}

	/** Constructor for missing value */
	public Speed() {
		super(true);
		speed_kph = 0;
	}

	/** Get the speed in client units.
	 * @return Speed in client units or null if missing. */
	public String toString2() {
		if(isMissing())
			return null;
		double t = (useSi() ? speed_kph : kphToMph(speed_kph));
		return SString.doubleToString(t, 0) + " " + getUnits();
	}

	/** Get units */
	public String getUnits() {
		return useSi() ? "km/h" : "mph";
	}

	/** Get the speed in KPH */
	public double toKph() {
		return (double)speed_kph;
	}

	/** Get the speed in mph */
	public double toMph() {
		return kphToMph(speed_kph);
	}

	/** Convert kph to mph */
	private double kphToMph(double kph) {
		return kph / Length.KM_PER_MILE;
	}
}
