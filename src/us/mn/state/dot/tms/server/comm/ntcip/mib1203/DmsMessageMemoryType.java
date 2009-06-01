/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
 * Ntcip DmsMessageMemoryType object
 *
 * @author Douglas Lau
 */
public class DmsMessageMemoryType extends ASN1Integer {

	/** Enumeration of memory types */
	static public enum Enum {
		undefined, other, permanent, changeable, _volatile,
		currentBuffer, schedule, blank;

		/** Get memory type from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return undefined;
		}
	}

	/** Test if a message memory type is "blank" */
	static public boolean isBlank(int m) {
		Enum mt = Enum.fromOrdinal(m);
	 	// Ledstar blank messages are undefined in dmsMsgTableSource
		return mt == Enum.blank || mt == Enum.undefined;
	}

	/** Memory type */
	protected final int memory;

	/** Message number */
	protected final int number;

	/** Create a new memory type object */
	public DmsMessageMemoryType(Enum m, int n) {
		memory = m.ordinal();
		number = n;
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIBNode.dmsMessageEntry.createOID(new int[] {
			1, memory, number});
	}
}
