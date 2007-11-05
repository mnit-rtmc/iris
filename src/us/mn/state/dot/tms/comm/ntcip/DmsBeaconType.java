/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2002  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.comm.ntcip;

/**
 * Ntcip DmsBeaconType object
 *
 * @author Douglas Lau
 */
public class DmsBeaconType extends DmsSignCfg implements ASN1Integer {

	/** Beacon types */
	static protected final String[] BEACON = {
		"?", "Other", "None", "One", "Two, synchronized",
		"Two, opposing", "Four, synchronized", "Four, alt. row",
		"Four, alt. column", "Four, alt. diagonal",
		"Four, not synchronized", "One, strobe", "Two, strobe",
		"Four, strobe"
	};

	/** Create a new DmsBeaconType object */
	public DmsBeaconType() {
		super(8);
	}

	/** Get the object name */
	protected String getName() { return "dmsBeaconType"; }

	/** Beacon type */
	protected int type;

	/** Set the integer value */
	public void setInteger(int value) {
		type = value;
		if(type < 0 || type >= BEACON.length) type = 0;
	}

	/** Get the integer value */
	public int getInteger() { return type; }

	/** Get the object value */
	public String getValue() { return BEACON[type]; }
}
