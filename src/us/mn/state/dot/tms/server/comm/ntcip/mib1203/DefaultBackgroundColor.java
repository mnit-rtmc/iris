/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.ColorClassic;
import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;

/**
 * Ntcip DefaultBackgroundColor object
 *
 * @author Douglas Lau
 */
public class DefaultBackgroundColor extends ASN1Integer {

	/** Create a new DefaultBackgroundColor object */
	public DefaultBackgroundColor() {
		super(MIB1203.multiCfg.create(new int[] {1, 0}));
	}

	/** Get the enum value */
	public ColorClassic getEnum() {
		return ColorClassic.fromOrdinal(value);
	}

	/** Get the object value */
	public String getValue() {
		ColorClassic c = getEnum();
		if(c != null)
			return c.toString();
		else
			return String.valueOf(value);
	}
}
