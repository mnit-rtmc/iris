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
 * FontNumber
 *
 * @author Douglas Lau
 */
public class FontNumber extends ASN1Integer {

	/** Font index */
	protected final int font;

	/** Create a new font number object */
	public FontNumber(int f) {
		font = f;
	}

	/** Create a new font number object */
	public FontNumber(int f, int n) {
		font = f;
		value = n;
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIBNode.fontDefinition.createOID(new int[] {
			2, 1, 2, font});
	}
}
