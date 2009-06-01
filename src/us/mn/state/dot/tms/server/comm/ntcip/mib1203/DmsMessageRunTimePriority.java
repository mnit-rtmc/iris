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
 * Ntcip DmsMessageRunTimePriority object
 *
 * @author Douglas Lau
 */
public class DmsMessageRunTimePriority extends ASN1Integer {

	/** Memory type */
	protected final int memory;

	/** Message number */
	protected final int number;

	/** Create a new DmsMessageRunTimePriority object */
	public DmsMessageRunTimePriority(DmsMessageMemoryType.Enum m, int n) {
		memory = m.ordinal();
		number = n;
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIBNode.dmsMessageEntry.createOID(new int[] {
			8, memory, number});
	}
}
