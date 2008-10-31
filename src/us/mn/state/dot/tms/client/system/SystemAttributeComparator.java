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
package us.mn.state.dot.tms.client.system;

import java.util.Comparator;
import us.mn.state.dot.tms.SystemAttribute;

/**
 * Comparator for sorting system attributes.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SystemAttributeComparator implements Comparator<SystemAttribute> {

	/** Compare one SystemAttribute with another */
	public int compare(SystemAttribute t0, SystemAttribute t1) {

		// attribute name
		String i0 = t0.getName();
		String i1 = t1.getName();
		int c = i0.compareTo(i1);
		if(c != 0)
			return c;

		// attribute value
		String s0 = t0.getValue();
		String s1 = t1.getValue();
		return s0.compareTo(s1);
	}

	/** Check equality */
	public boolean equals(Object o) {
		return o == this;
	}
}
