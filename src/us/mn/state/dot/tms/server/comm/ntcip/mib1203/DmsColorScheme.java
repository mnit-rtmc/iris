/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;

/**
 * DmsColorScheme indicates which color scheme is supported by the sign.
 *
 * @author Douglas Lau
 */
public class DmsColorScheme extends ASN1Integer {

	/** Enumeration of color schemes */
	static public enum Enum {
		undefined, monochrome1bit, monochrome8bit,
		colorClassic, color24bit;

		/** Get color scheme from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return undefined;
		}

		/** Lookup an enum from bpp */
		static public Enum fromBpp(int bpp) {
			switch(bpp) {
			case 1:
				return monochrome1bit;
			case 4:
				// FIXME: add support for colorClassic scheme
				return colorClassic;
			case 8:
				return monochrome8bit;
			case 24:
				return color24bit;
			}
			return undefined;
		}
	}

	/** Create a new DmsColorScheme object */
	public DmsColorScheme() {
		super(MIB1203.multiCfg.create(new int[] {11, 0}));
	}

	/** Get the enum value */
	public Enum getEnum() {
		return Enum.fromOrdinal(value);
	}

	/** Get the object value */
	public String getValue() {
		return Enum.fromOrdinal(value).toString();
	}
}
