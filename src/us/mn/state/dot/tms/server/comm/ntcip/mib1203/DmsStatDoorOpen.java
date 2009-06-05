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
 * Ntcip DmsStatDoorOpen object
 *
 * @author Douglas Lau
 */
public class DmsStatDoorOpen extends ASN1Integer {

	/** Create a new DmsStatDoorOpen object */
	public DmsStatDoorOpen() {
		super(MIB1203.dmsStatus.create(new int[] {6, 0}));
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < 8; i++) {
			int bit = 1 << i;
			if((value & bit) != 0) {
				b.append("#");
				b.append(i + 1);
				b.append(", ");
			}
		}
		if(b.length() == 0)
			b.append("None");
		else {
			// remove trailing comma and space
			b.setLength(b.length() - 2);
		}
		return b.toString();
	}
}
