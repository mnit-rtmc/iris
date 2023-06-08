/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2023  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.units.Temperature.Units.CELSIUS;
import us.mn.state.dot.tms.utils.Json;

/**
 * Temperature object in tenths of a degree C.
 *
 * @author Douglas Lau
 */
public class TemperatureObject {

	/** A value of 1001 indicates an error condition or missing value */
	static private final int ERROR_MISSING = 1001;

	/** Json speed key */
	private final String key;

	/** Integer MIB node */
	public final ASN1Integer node;

	/** Create a temperature object */
	public TemperatureObject(String k, ASN1Integer n) {
		key = k;
		node = n;
		node.setInteger(ERROR_MISSING);
	}

	/** Get value as Temperature.
	 * @return Temperature or null if missing */
	public Temperature getTemperature() {
		int t = node.getInteger();
		return (t != ERROR_MISSING)
		      ? new Temperature(0.1 * (double) t)
		      : null;
	}

	/** Get value as degrees celcius */
	public Integer getTempC() {
		Temperature t = getTemperature();
		return (t != null) ? t.round(CELSIUS) : null;
	}

	/** Get JSON representation */
	private Double tempC() {
		int t = node.getInteger();
		return (t != ERROR_MISSING) ? (double) t * 0.1 : null;
	}

	/** Get JSON representation */
	public String toJson() {
		Double t = tempC();
		if (t != null) {
			// Format temp C to 1 decimal place
			return Json.num(key, Num.format(t, 1));
		} else
			return "";
	}
}
