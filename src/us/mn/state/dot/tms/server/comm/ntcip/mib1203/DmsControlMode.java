/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;

/**
 * Ntcip DmsControlMode object
 *
 * @author Douglas Lau
 */
public class DmsControlMode extends ASN1Integer {

	/** Enumeration of message control modes */
	static public enum Enum {
		undefined,
		other,			// deprecated in v2
		local,
		external,		// deprecated in v2
		central,
		centralOverride,
		simulation;		// deprecated in v2

		/** Get message control mode from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for (Enum e: values()) {
				if (e.ordinal() == o)
					return e;
			}
			return undefined;
		}
	}

	/** Create a new DmsControlMode object */
	public DmsControlMode() {
		super(MIB1203.signControl.child(new int[] {1, 0}));
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
