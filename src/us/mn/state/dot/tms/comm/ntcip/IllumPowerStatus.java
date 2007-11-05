/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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

	/** Gray RGB color constant */
	static protected final int GRAY = 0x888888;

	/** Red RGB color constant */
	static protected final int RED = 0xFF8888;

	/** Yellow RGB color constant */
	static protected final int YELLOW = 0xFFFF88;

	/** Green RGB color constant */
	static protected final int GREEN = 0x88FF88;

	/** Background colors */
	static protected final int[] COLOR = {
		GRAY, RED, YELLOW, GREEN, YELLOW, RED
	};

	/** Create a new IllumPowerStatus object */
	public IllumPowerStatus() {
		super(2);
		oid[node++] = 2;
		oid[node++] = 0;
	}

	/** Get the object name */
	protected String getName() { return "illumPowerStatus"; }

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
	public byte[] getOctetString() { return power; }

	/** Get the object value */
	public String getValue() {
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < power.length; i++) {
			if(buffer.length() > 0) buffer.append(", ");
			buffer.append("#");
			buffer.append(i + 1);
			buffer.append(": ");
			buffer.append(STATUS[power[i]]);
		}
		if(buffer.length() == 0) buffer.append("None");
		return buffer.toString();
	}

	/** Get status strings for a StatusTable */
	public String[] getStatus() {
		String[] rows = new String[power.length];
		for(int i = 0; i < power.length; i++) {
			rows[i] = STATUS[power[i]];
		}
		return rows;
	}

	/** Get background colors for a StatusTable */
	public int[] getBackground() {
		int[] b = new int[power.length];
		for(int i = 0; i < power.length; i++) {
			b[i] = COLOR[power[i]];
		}
		return b;
	}
}
