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

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Int;

/**
 * LedBadPixelLimit object is the number of failed pixels needed before the
 * sign will refuse to activate a message (with dmsActivateMsgError.OTHER).
 * Setting this to zero disables the shortErrorStatus.PIXEL error reporting.
 *
 * @author Douglas Lau
 */
public class LedBadPixelLimit extends ASN1Int {

	/** Create a new LedBadPixelLimit object */
	public LedBadPixelLimit() {
		this(500);
	}

	/** Create a new LedBadPixelLimit object */
	public LedBadPixelLimit(int l) {
		value = l;
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIBNode.ledstarSignControl.createOID(new int[] {3, 0});
	}
}
