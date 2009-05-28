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
public class DmsStatDoorOpen extends DmsStatus implements ASN1Integer {

	/** Create a new DmsStatDoorOpen object */
	public DmsStatDoorOpen() {
		super(2);
		oid[node++] = 6;
		oid[node++] = 0;
	}

	/** Get the object name */
	protected String getName() {
		return "dmsStatDoorOpen";
	}

	/** Door open status bitmap */
	protected int open;

	/** Set the integer value */
	public void setInteger(int value) {
		open = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return open;
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < 8; i++) {
			if(((open >> i) & 1) == 1) {
				if(b.length() > 0)
					b.append(", ");
				b.append("#");
				b.append(i + 1);
			}
		}
		if(b.length() == 0)
			b.append("None");
		return b.toString();
	}
}
