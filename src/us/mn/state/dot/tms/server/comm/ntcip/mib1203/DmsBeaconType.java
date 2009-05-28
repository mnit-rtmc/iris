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
	protected String getName() {
		return "dmsBeaconType";
	}

	/** Beacon type */
	protected int type;

	/** Set the integer value */
	public void setInteger(int value) {
		if(value < 0 || value >= BEACON.length)
			type = 0;
		else
			type = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return type;
	}

	/** Get the object value */
	public String getValue() {
		return BEACON[type];
	}
}
