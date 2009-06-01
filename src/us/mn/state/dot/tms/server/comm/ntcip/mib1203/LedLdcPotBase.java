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
 * LedLdcPotBase object
 *
 * @author Douglas Lau
 */
public class LedLdcPotBase extends ASN1Integer {

	/** Create a new LedLdcPotBase object */
	public LedLdcPotBase() {
		this(0);
	}

	/** Create a new LedLdcPotBase object */
	public LedLdcPotBase(int b) {
		value = b;
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIB1203.ledstarSignControl.createOID(new int[] {6, 0});
	}
}
