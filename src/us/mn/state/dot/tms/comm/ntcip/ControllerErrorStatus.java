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
 * Ntcip ControllerErrorStatus object
 *
 * @author Douglas Lau
 */
public class ControllerErrorStatus extends StatError implements ASN1Integer {

	/** Other error */
	static public final int OTHER = 1 << 0;

	/** PROM error */
	static public final int PROM = 1 << 1;

	/** Program/processor error */
	static public final int PROCESSOR = 1 << 2;

	/** RAM error */
	static public final int RAM = 1 << 3;

	/** Error descriptions */
	static protected final String ERROR[] = {
		"OTHER", "PROM", "PROGRAM/PROCESSOR", "RAM"
	};

	/** Create a new ControllerErrorStatus object */
	public ControllerErrorStatus() {
		super(2);
		oid[node++] = 10;
		oid[node++] = 0;
	}

	/** Get the object name */
	protected String getName() { return "controllerErrorStatus"; }

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
				buffer.append(ERROR[i] + " ERROR");
			}
		}
		if(buffer.length() == 0) buffer.append("OK");
		return buffer.toString();
	}
}
