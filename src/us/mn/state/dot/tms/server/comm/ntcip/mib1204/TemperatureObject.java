/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2025  Minnesota Department of Transportation
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
public class TemperatureObject extends IntegerObject {

	/** Create a temperature object */
	public TemperatureObject(String k, ASN1Integer n) {
		super(k, n);
	}

	/** Get the minimum valid value */
	@Override
	protected int minValue() {
		return -1000;
	}

	/** Get the maximum valid value */
	@Override
	protected int maxValue() {
		return 1000;
	}

	/** Get value as Temperature.
	 * @return Temperature or null if missing */
	public Temperature getTemperature() {
		Integer t = getValue();
		return (t != null) ? new Temperature(0.1 * (double) t) : null;
	}

	/** Get value as degrees celcius */
	public Integer getTempC() {
		Temperature t = getTemperature();
		return (t != null) ? t.round(CELSIUS) : null;
	}

	/** Get JSON representation */
	@Override
	public String toJson() {
		Integer t = getValue();
		String v = (t != null) ? Num.format((double) t * 0.1, 1) : null;
		return Json.num(key, v);
	}
}
