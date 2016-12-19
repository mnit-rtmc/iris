/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009 AHMCT, University of California
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.util.Comparator;
import us.mn.state.dot.tms.utils.SString;

/**
 * Comparator for objects that can be alpha, numeric, or a mixture.
 * If both objects are numeric, they are compared as numbers, otherwise
 * as strings. Numerically equal arguments are compared as strings. For
 * clarity, see the test cases.
 * @author Michael Darter
 * @see NumericAlphaComparatorTest
 */
public class NumericAlphaComparator<T> implements Comparator<T> {

	/** Compare two strings */
	static public int compareStrings(String arg_a, String arg_b) {
		String a = (arg_a != null) ? arg_a : "null";
		String b = (arg_b != null) ? arg_b : "null";
		if (SString.isNumeric(a) && SString.isNumeric(b)) {
			int d = SString.stringToInt(a) -
			        SString.stringToInt(b);
			return (d != 0) ? d : a.compareTo(b);
		}
		// ignore common alpha prefix
		int pl = SString.alphaPrefixLen(a, b);
		if (pl > 0) {
			a = (pl < a.length()) ? a.substring(pl) : "";
			b = (pl < b.length()) ? b.substring(pl) : "";
			return compareStrings(a, b);	// recursive
		}
		return a.compareTo(b);
	}

	/** Compare two objects */
	@Override
	public int compare(T a, T b) {
		String sa = (a != null) ? a.toString() : null;
		String sb = (b != null) ? b.toString() : null;
		return compareStrings(sa, sb);
	}

	/** Check equality */
	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	/** hashCode */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
