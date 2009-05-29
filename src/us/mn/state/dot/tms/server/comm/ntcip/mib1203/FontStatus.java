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

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Int;

/**
 * Ntcip FontStatus object.  This object was added in 1203 v2.
 *
 * @author Douglas Lau
 */
public class FontStatus extends ASN1Int {

	/** Enumeration of font status values */
	static public enum Enum {
		undefined, notUsed, modifying, calculatingID, readyForUse,
		inUse, permanent, modifyReq, readyForUseReq, notUsedReq,
		unmanagedReq, unmanaged;

		/** Get font status from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return undefined;
		}
	}

	/** Font index */
	protected final int font;

	/** Create a new FontStatus object */
	public FontStatus(int f) {
		font = f;
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIBNode.fontDefinition.createOID(new int[] {
			2, 1, 8, font});
	}

	/** Set the integer value */
	public void setInteger(int v) {
		value = Enum.fromOrdinal(v).ordinal();
	}

	/** Get the object value */
	public String getValue() {
		return Enum.fromOrdinal(value).toString();
	}

	/** Get the enum value */
	public Enum getEnum() {
		return Enum.fromOrdinal(value);
	}
}
