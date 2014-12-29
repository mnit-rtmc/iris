/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.util.Comparator;
import us.mn.state.dot.tms.SignText;

/**
 * Comparator for sorting sign text messages.
 *
 * @author Douglas Lau
 */
public class SignTextComparator implements Comparator<SignText> {

	/** Compare one sign text message with another */
	@Override
	public int compare(SignText st0, SignText st1) {
		int c = compareLine(st0, st1);
		if (c == 0)
			c = compareRank(st0, st1);
		if (c == 0)
			c = compareMulti(st0, st1);
		if (c == 0)
			c = compareName(st0, st1);
		return c;
	}

	/** Compare line numbers */
	private int compareLine(SignText st0, SignText st1) {
		Short l0 = st0.getLine();
		Short l1 = st1.getLine();
		return l0.compareTo(l1);
	}

	/** Compare ranks */
	private int compareRank(SignText st0, SignText st1) {
		Short r0 = st0.getRank();
		Short r1 = st1.getRank();
		return r0.compareTo(r1);
	}

	/** Compare multi strings */
	private int compareMulti(SignText st0, SignText st1) {
		return st0.getMulti().compareTo(st1.getMulti());
	}

	/** Compare names */
	private int compareName(SignText st0, SignText st1) {
		return st0.getName().compareTo(st1.getName());
	}
}
