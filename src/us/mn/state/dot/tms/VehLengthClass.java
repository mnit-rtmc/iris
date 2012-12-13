/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.FEET;

/**
 * Vehicle length class.
 *
 * @author Douglas Lau
 */
public enum VehLengthClass {
	MOTORCYCLE(new Distance(0, FEET), new Distance(7, FEET)),
	SHORT(new Distance(7, FEET), new Distance(20, FEET)),
	MEDIUM(new Distance(20, FEET), new Distance(43, FEET)),
	LONG(new Distance(43, FEET), new Distance(255, FEET));

	/** Lower bound of vehicle length */
	public final Distance lower_bound;

	/** Upper bound of vehicle length */
	public final Distance upper_bound;

	/** Create a new vehicle length class */
	private VehLengthClass(Distance lb, Distance ub) {
		lower_bound = lb;
		upper_bound = ub;
	}

	/** Size of enum */
	static public final int size = values().length;

	/** Get a vehicle length class from an ordinal */
	static public VehLengthClass fromOrdinal(int o) {
		for(VehLengthClass vc: VehLengthClass.values()) {
			if(vc.ordinal() == o)
				return vc;
		}
		return SHORT;
	}
}
