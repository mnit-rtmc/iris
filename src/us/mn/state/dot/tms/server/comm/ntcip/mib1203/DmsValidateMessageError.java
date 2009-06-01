/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2009  Minnesota Department of Transportation
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
 * Ntcip DmsValidateMessageError object
 *
 * @author Douglas Lau
 */
public class DmsValidateMessageError extends ASN1Integer {

	/** Enumeration of message validation errors */
	static public enum Enum {
		undefined, other, none, beacons, pixelService, syntaxMULTI;

		/** Get message validation error from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return undefined;
		}
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIB1203.dmsMessage.createOID(new int[] {9, 0});
	}

	/** Set the integer value */
	public void setInteger(int v) {
		value = Enum.fromOrdinal(v).ordinal();
	}

	/** Get the object value */
	public String getValue() {
		return Enum.fromOrdinal(value).toString();
	}

	/** Test for a MULTI syntax error */
	public boolean isSyntaxMulti() {
		return Enum.fromOrdinal(value) == Enum.syntaxMULTI;
	}

	/** Test if there is an error */
	public boolean isError() {
		return Enum.fromOrdinal(value) != Enum.none;
	}
}
