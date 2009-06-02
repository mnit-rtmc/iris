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
 * DmsGraphicStatus indicates the current status of the graphic.
 *
 * @author Douglas Lau
 */
public class DmsGraphicStatus extends ASN1Integer {

	/** Enumeration of graphic status values */
	static public enum Enum {
		undefined, notUsed, modifying, calculatingID, readyForUse,
		inUse, permanent, modifyReq, readyForUseReq, notUsedReq;

		/** Get graphic status from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return undefined;
		}
	}

	/** Create a new DmsGraphicStatus object */
	public DmsGraphicStatus(int row) {
		super(MIB1203.dmsGraphicEntry.create(new int[] {10, row}));
	}

	/** Set the enum value */
	public void setEnum(Enum v) {
		value = v.ordinal();
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
