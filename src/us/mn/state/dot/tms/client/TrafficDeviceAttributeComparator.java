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
package us.mn.state.dot.tms.client;

import java.util.Comparator;
import us.mn.state.dot.tms.TrafficDeviceAttribute;

/**
 * Comparator for sorting traffic device attributes.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class TrafficDeviceAttributeComparator implements Comparator<TrafficDeviceAttribute> {

	/** Compare one TrafficDeviceAttribute with another */
	public int compare(TrafficDeviceAttribute t0, TrafficDeviceAttribute t1) {

		// for same traffic device?
		String i0 = t0.getId();
		String i1 = t1.getId();
		int c = i0.compareTo(i1);
		if(c != 0)
			return c;

		// for same attribute name?
		String s0 = t0.getAttributeName();
		String s1 = t1.getAttributeName();
		c = s0.compareTo(s1);
		if(c != 0)
			return c;

		return c;
	}

	/** Check equality */
	public boolean equals(Object o) {
		return o == this;
	}
}
