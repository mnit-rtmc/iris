/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
 * PixelFailureDetectionType
 *
 * @author Douglas Lau
 */
public class PixelFailureDetectionType extends ASN1Integer {

	/** Enumeration of failure detection types */
	static public enum Enum {
		undefined, other, pixelTest, messageDisplay;

		/** Get failure detection type from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return undefined;
		}
	}

	/** Row in table */
	protected final int row;

	/** Create a new pixel failure detection type object */
	public PixelFailureDetectionType(int r) {
		row = r;
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIB1203.pixelFailureEntry.createOID(new int[] {1,2,row});
	}

	/** Set the integer value */
	public void setInteger(int v) {
		value = Enum.fromOrdinal(v).ordinal();
	}

	/** Get the object value */
	public String getValue() {
		return Enum.fromOrdinal(value).toString();
	}
}
