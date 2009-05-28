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
 * Ntcip VmsVerticalPitch object
 *
 * @author Douglas Lau
 */
public class VmsVerticalPitch extends VmsCfg implements ASN1Integer {

	/** Create a new VmsVerticalPitch object */
	public VmsVerticalPitch() {
		this(0);
	}

	/** Create a new VmsVerticalPitch object */
	public VmsVerticalPitch(int p) {
		super(6);
		pitch = p;
	}

	/** Get the object name */
	protected String getName() {
		return "vmsVerticalPitch";
	}

	/** Vertical pitch */
	protected int pitch;

	/** Set the integer value */
	public void setInteger(int value) {
		pitch = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return pitch;
	}

	/** Get the object value */
	public String getValue() {
		return String.valueOf(pitch);
	}
}
