/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.units.Speed;
import static us.mn.state.dot.tms.units.Speed.Units.KPH;
import static us.mn.state.dot.tms.units.Speed.Units.MPS;
import us.mn.state.dot.tms.utils.Json;

/**
 * Wind speed object.
 * Speeds are reported in tenths of meters per second.
 *
 * @author Douglas Lau
 */
public class WindSpeedObject extends IntegerObject {

	/** Create a wind speed object */
	public WindSpeedObject(String k, ASN1Integer n) {
		super(k, n);
	}

	/** Get value as MPS */
	private Double speedMPS() {
		Integer s = getValue();
		return (s != null) ? (double) s * 0.1 : null;
	}

	/** Get value as Speed.
	 * @return Speed or null if missing */
	public Speed getSpeed() {
		Double mps = speedMPS();
		return (mps != null) ? new Speed(mps, MPS) : null;
	}

	/** Get value as KPH */
	public Integer getSpeedKPH() {
		Speed s = getSpeed();
		return (s != null) ? s.round(KPH) : null;
	}

	/** Get JSON representation */
	@Override
	public String toJson() {
		Double mps = speedMPS();
		// Format speed to 1 decimal place
		String s = (mps != null) ? Num.format(mps, 1) : null;
		return Json.num(key, s);
	}
}
