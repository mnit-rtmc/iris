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

import java.text.NumberFormat;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.units.Speed;
import static us.mn.state.dot.tms.units.Speed.Units.KPH;
import static us.mn.state.dot.tms.units.Speed.Units.MPS;
import us.mn.state.dot.tms.utils.Json;

/**
 * Wind speed object.
 *
 * @author Douglas Lau
 */
public class WindSpeedObject {

	/** Wind speed of 65535 indicates error or missing value */
	static private final int ERROR_MISSING = 65535;

	/** Json speed key */
	private final String key;

	/** Integer MIB node */
	public final ASN1Integer node;

	/** Create a wind speed object */
	public WindSpeedObject(String k, ASN1Integer n) {
		key = k;
		node = n;
		node.setInteger(ERROR_MISSING);
	}

	/** Get value as MPS */
	private Double speedMPS() {
		// Speeds are recorded in tenths of meters per second
		int s = node.getInteger();
		return (s != ERROR_MISSING) ? (double) s * 0.1 : null;
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
	public String toJson() {
		Double mps = speedMPS();
		if (mps != null) {
			// Format speed to 1 decimal place
			NumberFormat f = NumberFormat.getInstance();
			f.setMaximumFractionDigits(1);
			f.setMinimumFractionDigits(1);
			return Json.num(key, f.format(mps));
		} else
			return "";
	}
}
