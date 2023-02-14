/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1204;

import java.text.NumberFormat;

/**
 * Number formatting utility.
 *
 * @author Douglas Lau
 */
public class Num {

	/** Create a number formatter for a given number of digits */
	static private NumberFormat createFormatter(int digits) {
		NumberFormat f = NumberFormat.getInstance();
		f.setGroupingUsed(false);
		f.setMaximumFractionDigits(digits);
		f.setMinimumFractionDigits(Math.min(1, digits));
		return f;
	}

	/** Format a Float to the given number of digits */
	static public String format(Float num, int digits) {
		return (num != null)
		      ? createFormatter(digits).format(num)
		      : null;
	}

	/** Format a Double to the given number of digits */
	static public String format(Double num, int digits) {
		return (num != null)
		      ? createFormatter(digits).format(num)
		      : null;
	}
}
