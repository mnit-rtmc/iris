/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2026  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.MsgLine;

/**
 * Comparator for sorting message lines.
 *
 * @author Douglas Lau
 */
public class MsgLineComparator implements Comparator<MsgLine> {

	/** Compare one message line with another */
	@Override
	public int compare(MsgLine ml0, MsgLine ml1) {
		int c = compareLine(ml0, ml1);
		if (c == 0)
			c = compareRank(ml0, ml1);
		if (c == 0)
			c = compareMulti(ml0, ml1);
		if (c == 0)
			c = compareName(ml0, ml1);
		return c;
	}

	/** Compare line numbers */
	private int compareLine(MsgLine ml0, MsgLine ml1) {
		Short l0 = ml0.getLine();
		Short l1 = ml1.getLine();
		return l0.compareTo(l1);
	}

	/** Compare ranks */
	private int compareRank(MsgLine ml0, MsgLine ml1) {
		Short r0 = ml0.getRank();
		Short r1 = ml1.getRank();
		return r0.compareTo(r1);
	}

	/** Compare multi strings */
	private int compareMulti(MsgLine ml0, MsgLine ml1) {
		return ml0.getMulti().compareTo(ml1.getMulti());
	}

	/** Compare names */
	private int compareName(MsgLine ml0, MsgLine ml1) {
		return ml0.getName().compareTo(ml1.getName());
	}
}
