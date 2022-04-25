/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.utils.Json;

/**
 * Radiation object in watts / m^2.
 *
 * @author Douglas Lau
 */
public class RadiationObject {

	/** Value of 2049 indicates error or missing value */
	static private final int ERROR_MISSING = 2049;

	/** Json radiation object key */
	private final String key;

	/** Integer MIB node */
	public final ASN1Integer node;

	/** Create a radiation object */
	public RadiationObject(String k, ASN1Integer n) {
		key = k;
		node = n;
		node.setInteger(ERROR_MISSING);
	}

	/** Get radiation value (watts / m^2).
	 * @return Radiation or null if missing */
	public Integer getRadiation() {
		int r = node.getInteger();
		return (r >= -2048 && r < ERROR_MISSING) ? r : null;
	}

	/** Get JSON representation */
	public String toJson() {
		return Json.num(key, getRadiation());
	}
}
