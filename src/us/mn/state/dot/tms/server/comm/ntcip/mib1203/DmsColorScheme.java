/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

/**
 * DmsColorScheme indicates which color scheme is supported by the sign.
 * This object was added in 1203v2.
 *
 * @author Douglas Lau
 */
public enum DmsColorScheme {
	undefined	(0),
	monochrome1bit	(1),
	monochrome8bit	(8),
	colorClassic	(4),
	color24bit	(24);

	/** Number of bits per pixel */
	public final int bpp;

	/** Create a new color scheme */
	private DmsColorScheme(int b) {
		bpp = b;
	}

	/** Lookup from bpp */
	static public DmsColorScheme fromBpp(int bpp) {
		for (DmsColorScheme s: values()) {
			if (s.bpp == bpp)
				return s;
		}
		return undefined;
	}
}
