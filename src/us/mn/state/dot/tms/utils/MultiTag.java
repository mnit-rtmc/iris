/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2023  Minnesota Department of Transportation
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

import java.util.ArrayList;

/**
 * Enumeration of MULTI tags.  This is specified by NTCIP 1203, as part of the
 * dmsSupportedMultiTags object.
 *
 * The ordinal values correspond to the bits in the iris.multi_tag look-up
 * table.
 *
 * @author Douglas Lau
 */
public enum MultiTag {
	cb,  //  0: color -- background
	cf,  //  1: color -- foreground
	fl,  //  2: flash
	fo,  //  3: font
	g,   //  4: graphic
	hc,  //  5: hexadecimal character
	jl,  //  6: justification -- line
	jp,  //  7: justification -- page
	ms,  //  8: manufacturer specific
	mv,  //  9: moving text
	nl,  // 10: new line
	np,  // 11: new page
	pt,  // 12: page time
	sc,  // 13: spacing character
	f1,  // 14: field 1 (local time -- 12 hour)
	f2,  // 15: field 2 (local time -- 24 hour)
	f3,  // 16: field 3 (ambient temperature -- celsius)
	f4,  // 17: field 4 (ambient temperature -- fahrenheit)
	f5,  // 18: field 5 (speed -- kilometers per hour)
	f6,  // 19: field 6 (speed -- miles per hour)
	f7,  // 20: field 7 (day of week)
	f8,  // 21: field 8 (day of month)
	f9,  // 22: field 9 (month of year)
	f10, // 23: field 10 (year -- 2 digits)
	f11, // 24: field 11 (year -- 4 digits)
	f12, // 25: field 12 (local time -- 12 hour AM/PM)
	f13, // 26: field 13 (local time -- 12 hour am/pm)
	tr,  // 27: text rectangle
	cr,  // 28: color rectangle
	pb;  // 29: page background

	/** Enumerated values */
	static private final MultiTag[] VALUES = values();

	/** Get MULTI tag from an ordinal value */
	static public MultiTag fromOrdinal(int o) {
		for (MultiTag t: VALUES) {
			if (t.ordinal() == o)
				return t;
		}
		return null;
	}

	/** Get an array of tags from bit flags.
	 * @param v Bit flags.
	 * @return Array of MULTI tag values. */
	static public MultiTag[] getTags(int v) {
		ArrayList<MultiTag> tags = new ArrayList<MultiTag>();
		for (int i = 0; i < 32; i++) {
			if (((1 << i) & v) != 0) {
				MultiTag tag = fromOrdinal(i);
				if (tag != null)
					tags.add(tag);
			}
		}
		return tags.toArray(new MultiTag[0]);
	}

	/** Get MULTI tags as string from bit flags */
	static public String asString(int v) {
		StringBuilder b = new StringBuilder();
		for (MultiTag tag : getTags(v)) {
			b.append(tag.toString());
			b.append(',');
		}
		if (b.length() == 0)
			return "None";
		else {
			// remove trailing comma
			b.setLength(b.length() - 1);
			return b.toString();
		}
	}
}
