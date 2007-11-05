/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2002  Minnesota Department of Transportation
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
 * Ntcip ShortErrorStatus object
 *
 * @author Douglas Lau
 */
public class ShortErrorStatus extends StatError implements ASN1Integer {

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

	/** Fan error */
	static public final int FAN = 1 << 10;

	/** Error descriptions */
	static protected final String ERROR[] = {
		"OTHER", "COMMUNICATIONS", "POWER", "ATTACHED DEVICE", "LAMP",
		"PIXEL", "PHOTOCELL", "MESSAGE", "CONTROLLER", "TEMPERATURE",
		"FAN"
	};

	/** Create a new ShortErrorStatus object */
	public ShortErrorStatus() {
		super(2);
		oid[node++] = 1;
		oid[node++] = 0;
	}

	/** Get the object name */
	protected String getName() { return "shortErrorStatus"; }

	/** Short error status bitfield */
	protected int status;

	/** Set the integer value */
	public void setInteger(int value) { status = value; }

	/** Get the integer value */
	public int getInteger() { return status; }

	/** Get the object value */
	public String getValue() {
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < ERROR.length; i++) {
			if((status & 1 << i) != 0) {
				if(buffer.length() > 0) buffer.append(", ");
				buffer.append(ERROR[i]);
			}
		}
		if(buffer.length() == 0) buffer.append("OK");
		else buffer.append(" ERROR");
		return buffer.toString();
	}

	/** Check if an error bit is set */
	public boolean checkError(int mask) {
		return (status & mask) > 0;
	}
}
