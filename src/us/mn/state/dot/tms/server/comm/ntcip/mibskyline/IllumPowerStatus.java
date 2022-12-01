/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mibskyline;

import org.json.JSONArray;
import org.json.JSONObject;
import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;

/**
 * Skyline IllumPowerStatus object
 *
 * @author Douglas Lau
 */
public class IllumPowerStatus extends ASN1OctetString {

	/** Enumeration of power status */
	static public enum Enum {
		unavailable, low, marginallyLow, ok, marginallyHigh, high;

		/** Get power status from an ordinal value */
		static protected Enum fromOrdinal(int o) {
			for (Enum e: values()) {
				if (e.ordinal() == o)
					return e;
			}
			return unavailable;
		}

		/** Get value */
		private String value() {
			switch (this) {
			case low:
			case high:
				return "powerFail";
			case marginallyLow:
			case marginallyHigh:
				return "voltageOutOfSpec";
			default:
				return "noError";
			}
		}
	}

	/** Create a new IllumPowerStatus object */
	public IllumPowerStatus() {
		super(MIB.illumPowerStatus.node);
	}

	/** Get the object value */
	@Override
	public String getValue() {
		byte[] v = getByteValue();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < v.length; i++) {
			sb.append(", #");
			sb.append(i + 1);
			sb.append(": ");
			sb.append(Enum.fromOrdinal(v[i]));
		}
		if (sb.length() < 2)
			return "None";
		else
			return sb.substring(2);
	}

	/** Get power status for all power supplies. */
	public JSONArray getPowerStatus() {
		byte[] vals = getByteValue();
		if (vals.length == 0)
			return null;
		JSONArray supplies = new JSONArray();
		for (int i = 0; i < vals.length; i++)
			supplies.put(getPowerStatus(vals, i));
		return supplies;
	}

	/** Get the power status for one power supply */
	private JSONObject getPowerStatus(byte[] vals, int num) {
		JSONObject supply = new JSONObject();
		supply.put("description", "#" + (num + 1));
		supply.put("supply_type", "ledSupply"); // 1203v2 dmsPowerType
		Enum e = Enum.fromOrdinal(vals[num]);
		supply.put("power_status", e.value());
		supply.put("detail", "skyline");
		return supply;
	}

	/** Check if the power status is critical */
	public boolean isCritical() {
		byte[] vals = getByteValue();
		int n_failed = 0;
		for (byte v: vals) {
			switch (Enum.fromOrdinal(v)) {
			case low:
			case high:
				n_failed++;
			default:
				break;
			}
		}
		return 2 * n_failed > vals.length;
	}
}
