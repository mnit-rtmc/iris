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

import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Physical quantity base class.
 *
 * @author Michael Darter
 */
abstract class PhysicalQuantity {

	/** String displayed for the user if value is missing */
	public static final String MISSING = "???";

	/** True if value is missing */
	private final boolean missing_val;

	/** Constructor
	 * @param m True if the value is missing. */
	protected PhysicalQuantity(boolean m) {
		missing_val = m;
	}

	/** Is angle missing? */
	public boolean isMissing() {
		return missing_val;
	}

	/** Get the physical quantity in client units as a string.
	 * @return The physical quantity in client units + client units 
	 *	    or MISSING if missing. For example "23.3 km/h" */
	public String toString() {
		return missing_val ? MISSING : toString2();
	}

	/** Get the physical quantity in client units as a string.
	 * @return The physical quantity in client units + client units 
	 *	    or null if missing. For example "23.3 km/h"*/
	public abstract String toString2();

	/** Get units as a string, e.g. "km/h" */
	public abstract String getUnits();

	/** Use SI or US customary units? */
	static public boolean useSi() {
		return SystemAttrEnum.CLIENT_UNITS_SI.getBoolean();
	}
}
