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
 * Ntcip CharacterWidth object
 *
 * @author Douglas Lau
 */
public class CharacterWidth extends ASN1Integer {

	/** Font index */
	protected final int font;

	/** Character index */
	protected final int index;

	/** Create a new CharacterWidth object */
	public CharacterWidth(int f, int i, int w) {
		font = f;
		index = i;
		value = w;
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIB1203.characterEntry.createOID(new int[] {
			2, font, index});
	}
}
