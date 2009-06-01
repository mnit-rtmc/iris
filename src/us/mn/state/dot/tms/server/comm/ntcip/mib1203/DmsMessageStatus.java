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
 * Ntcip DmsMessageStatus object
 *
 * @author Douglas Lau
 */
public class DmsMessageStatus extends ASN1Integer {

	/** Enumeration of font status values */
	static public enum Enum {
		undefined, notUsed, modifying, validating, valid, error,
		modifyReq, validateReq, notUsedReq;

		/** Get font status from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return undefined;
		}
	}

	/** Memory type */
	protected final int memory;

	/** Message number */
	protected final int number;

	/** Create a new DmsMessageStatus object */
	public DmsMessageStatus(DmsMessageMemoryType.Enum m, int n, Enum s) {
		memory = m.ordinal();
		number = n;
		value = s.ordinal();
	}

	/** Create a new DmsMessageStatus object */
	public DmsMessageStatus(DmsMessageMemoryType.Enum m, int n) {
		memory = m.ordinal();
		number = n;
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIB1203.dmsMessageEntry.createOID(new int[] {
			9, memory, number});
	}

	/** Set the integer value */
	public void setInteger(int v) {
		value = Enum.fromOrdinal(v).ordinal();
	}

	/** Get the object value */
	public String getValue() {
		return Enum.fromOrdinal(value).toString();
	}

	/** Is the status "modifying"? */
	public boolean isModifying() {
		return Enum.fromOrdinal(value) == Enum.modifying;
	}

	/** Is the status "valid"? */
	public boolean isValid() {
		return Enum.fromOrdinal(value) == Enum.valid;
	}
}
