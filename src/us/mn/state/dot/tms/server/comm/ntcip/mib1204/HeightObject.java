/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1204;

import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.METERS;
import us.mn.state.dot.tms.utils.Json;

/**
 * Height object in meters.
 *
 * @author Douglas Lau
 */
public class HeightObject {

	/** A height of 1001 is an error condition or missing value */
	static private final int ERROR_MISSING = 1001;

	/** Json speed key */
	private final String key;

	/** Integer MIB node */
	public final ASN1Integer node;

	/** Create a height object */
	public HeightObject(String k, ASN1Integer n) {
		key = k;
		node = n;
		node.setInteger(ERROR_MISSING);
	}

	/** Get height as Distance.
	 * @return Distance or null if missing */
	public Distance getHeight() {
		int h = node.getInteger();
		return (h < ERROR_MISSING) ? new Distance(h, METERS) : null;
	}

	/** Get sensor height in meters */
	public Integer getHeightM() {
		Distance h = getHeight();
		return (h != null) ? h.round(METERS) : null;
	}

	/** Get JSON representation */
	public String toJson() {
		return Json.num(key, getHeightM());
	}
}
