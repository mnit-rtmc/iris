/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
 * PixelFailureIndex
 *
 * @author Douglas Lau
 */
public class PixelFailureIndex extends ASN1Integer {

	/** Row in table */
	protected final int row;

	/** Create a new pixel failure index object */
	public PixelFailureIndex(int r) {
		row = r;
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIBNode.pixelFailureEntry.createOID(new int[] {2,2,row});
	}
}
