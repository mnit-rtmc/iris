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
 * Direction object.
 *
 * @author Douglas Lau
 */
public class DirectionObject {

	/** Dir of 361 indicates error or missing value */
	static private final int ERROR_MISSING = 361;

	/** Json direction key */
	private final String key;

	/** Integer MIB node */
	public final ASN1Integer node;

	/** Create a direction object */
	public DirectionObject(String k, ASN1Integer n) {
		key = k;
		node = n;
		node.setInteger(ERROR_MISSING);
	}

	/** Get direction as degrees */
	public Integer getDirection() {
		int d = node.getInteger();
	 	// Direction is degrees clockwise from north,
	 	// with 361 indicating an error or missing value.
		return (d >= 0 && d <= 360) ? Integer.valueOf(d) : null;
	}

	/** Get JSON representation */
	public String toJson() {
		return Json.num(key, getDirection());
	}
}
