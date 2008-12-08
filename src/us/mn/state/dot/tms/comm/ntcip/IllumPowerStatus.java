/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.ntcip;

/**
 * Ntcip IllumPowerStatus object
 *
 * @author Douglas Lau
 */
public class IllumPowerStatus extends SkylineDmsStatus
	implements ASN1OctetString
{
	/** Power status codes */
	static public final int UNAVAILABLE = 0;
	static public final int LOW = 1;
	static public final int MARGINALLY_LOW = 2;
	static public final int OK = 3;
	static public final int MARGINALLY_HIGH = 4;
	static public final int HIGH = 5;

	/** Status descriptions */
	static protected final String[] STATUS = {
		"???", "low", "marginally low", "OK", "marginally high", "high"
	};

	/** Create a new IllumPowerStatus object */
	public IllumPowerStatus() {
		super(2);
		oid[node++] = 2;
		oid[node++] = 0;
	}

	/** Get the object name */
	protected String getName() {
		return "illumPowerStatus";
	}

	/** Power status */
	protected byte[] power = new byte[0];

	/** Set the octet string value */
	public void setOctetString(byte[] value) {
		power = value;
		for(int i = 0; i < power.length; i++) {
			if(power[i] < 0 || power[i] >= STATUS.length)
				power[i] = UNAVAILABLE;
		}
	}

	/** Get the octet string value */
	public byte[] getOctetString() {
		return power;
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < power.length; i++) {
			if(b.length() > 0)
				b.append(", ");
			b.append("#");
			b.append(i + 1);
			b.append(": ");
			b.append(STATUS[power[i]]);
		}
		if(b.length() == 0)
			b.append("None");
		return b.toString();
	}

	/** Get status strings for a StatusTable */
	public String[] getStatus() {
		String[] rows = new String[power.length];
		for(int i = 0; i < power.length; i++)
			rows[i] = STATUS[power[i]];
		return rows;
	}
}
