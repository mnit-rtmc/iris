/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;

/**
 * Ntcip ShortErrorStatus object
 *
 * @author Douglas Lau
 */
public class ShortErrorStatus extends ASN1Integer {

	/** Other error */
	static public final int OTHER = 1 << 0;

	/** Communications error */
	static public final int COMMUNICATIONS = 1 << 1;

	/** Power error */
	static public final int POWER = 1 << 2;

	/** Attached device error */
	static public final int ATTACHED_DEVICE = 1 << 3;

	/** Lamp error */
	static public final int LAMP = 1 << 4;

	/** Pixel error */
	static public final int PIXEL = 1 << 5;

	/** Photocell error */
	static public final int PHOTOCELL = 1 << 6;

	/** Message error */
	static public final int MESSAGE = 1 << 7;

	/** Controller error */
	static public final int CONTROLLER = 1 << 8;

	/** Temperature warning */
	static public final int TEMPERATURE = 1 << 9;

	/** Climate control system error */
	static public final int CLIMATE_CONTROL = 1 << 10;

	/** Critical temperature error */
	static public final int CRITICAL_TEMPERATURE = 1 << 11;

	/** Drum sign rotor error */
	static public final int DRUM_ROTOR = 1 << 12;

	/** Door open alarm */
	static public final int DOOR_OPEN = 1 << 13;

	/** Humidity warning */
	static public final int HUMIDITY = 1 << 14;

	/** Error descriptions */
	static protected final String ERROR[] = {
		"OTHER", "COMMUNICATIONS", "POWER", "ATTACHED DEVICE", "LAMP",
		"PIXEL", "PHOTOCELL", "MESSAGE", "CONTROLLER", "TEMPERATURE",
		"CLIMATE CONTROL", "CRITICAL TEMPERATURE", "DRUM ROTOR",
		"DOOR OPEN", "HUMIDITY"
	};

	/** Mask of errors reportable for maintenance */
	static protected final int REPORTABLE_MASK =
		OTHER | COMMUNICATIONS | POWER | ATTACHED_DEVICE | LAMP |
		PHOTOCELL | CONTROLLER | TEMPERATURE | CRITICAL_TEMPERATURE |
		DOOR_OPEN | HUMIDITY;

	/** Get the object identifier */
	public int[] getOID() {
		return MIB1203.statError.createOID(new int[] {1, 0});
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder buf = new StringBuilder();
		for(int i = 0; i < ERROR.length; i++) {
			if((value & 1 << i) != 0) {
				if(buf.length() > 0)
					buf.append(", ");
				buf.append(ERROR[i]);
			}
		}
		if(buf.length() == 0)
			buf.append("OK");
		else
			buf.append(" ERROR");
		return buf.toString();
	}

	/** Check if an error bit is set */
	public boolean checkError(int mask) {
		return (value & mask) > 0;
	}

	/** Check if we should report the error for maintenance */
	public boolean shouldReport() {
		// PIXEL errors are reported by DMSQueryPixelStatus operation.
		// MESSAGE errors can pop up for lots of reasons,
		// so we shouldn't consider them real errors.
		// CLIMATE CONTROL errors are not reported because of too many
		// false positives from existing signs (Mn/DOT).
		return (value & REPORTABLE_MASK) != 0;
	}
}
