/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.units.Temperature;

/**
 * ASN1 temperature object.
 *
 * @author Douglas Lau
 */
public class TemperatureObject {

	/** A temperature of 1001 is an error condition or missing value */
	static private final int TEMP_ERROR_MISSING = 1001;

	/** Integer MIB node */
	public final ASN1Integer node;

	/** Create a temperature object */
	public TemperatureObject(ASN1Integer n) {
		node = n;
		node.setInteger(TEMP_ERROR_MISSING);
	}

	/** Get value as Temperature.
	 * @return Temperature or null if missing */
	public Temperature getTemperature() {
		int t = node.getInteger();
		return (t != TEMP_ERROR_MISSING)
		      ? new Temperature(0.1 * (double) t)
		      : null;
	}

	/** Get JSON representation */
	public String toJson() {
		int t = node.getInteger();
		return (t != TEMP_ERROR_MISSING)
		      ? Double.toString(0.1 * t)
		      : null;
	}

	/** Get JSON representation */
	public String toJson(String key) {
		String value = toJson();
		if (value != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("\"");
			sb.append(key);
			sb.append("\":");
			sb.append(value);
			sb.append(',');
			return sb.toString();
		} else
			return null;
	}
}
