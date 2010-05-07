/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2010  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.ntcip.ASN1OctetString;

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
			for(Enum e: Enum.values()) {
				if(e.ordinal() == o)
					return e;
			}
			return unavailable;
		}
	}

	/** Create a new IllumPowerStatus object */
	public IllumPowerStatus() {
		super(MIB.skylineDmsStatus.create(new int[] {2, 0}));
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < value.length; i++) {
			sb.append(", #");
			sb.append(i + 1);
			sb.append(": ");
			sb.append(Enum.fromOrdinal(value[i]));
		}
		if(sb.length() < 2)
			return "None";
		else
			return sb.substring(2);
	}

	/** Get power status for all power supplies.
	 * @see DMS.getPowerStatus */
	public String[] getPowerStatus() {
		String[] supplies = new String[value.length];
		for(int i = 0; i < value.length; i++)
			supplies[i] = getPowerStatus(i);
		return supplies;
	}

	/** Get the power status for one power supply */
	protected String getPowerStatus(int num) {
		byte v = value[num];
		StringBuilder sb = new StringBuilder();
		sb.append('#');
		sb.append(num + 1);
		sb.append(",ledSupply,");	// 1203v2 dmsPowerType
		Enum e = Enum.fromOrdinal(v);
		switch(e) {
		case low:
		case high:
			sb.append("powerFail,");
			sb.append(e);
			break;
		case marginallyLow:
		case marginallyHigh:
			sb.append("voltageOutOfSpec,");
			sb.append(e);
			break;
		default:
			sb.append(',');
			break;
		}
		return sb.toString();

	}
}
