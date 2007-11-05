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
 * Ntcip DmsSignAccess object
 *
 * @author Douglas Lau
 */
public class DmsSignAccess extends DmsSignCfg implements ASN1Integer {

	/** Other access (?) */
	static public final int OTHER = 1 << 0;

	/** Walk-in access */
	static public final int WALK_IN = 1 << 1;

	/** Back access */
	static public final int BACK = 1 << 2;

	/** Front access */
	static public final int FRONT = 1 << 3;

	/** Create a new DmsSignAccess object */
	public DmsSignAccess() {
		super(1);
	}

	/** Get the object name */
	protected String getName() { return "dmsSignAccess"; }

	/** Sign access bitfield */
	protected int access;

	/** Set the integer value */
	public void setInteger(int value) { access = value; }

	/** Get the integer value */
	public int getInteger() { return access; }

	/** Get the object value */
	public String getValue() {
		StringBuffer buffer = new StringBuffer();
		if((access & FRONT) > 0) buffer.append("Front");
		if((access & BACK) > 0) {
			if(buffer.length() > 0) buffer.append(", ");
			buffer.append("Back");
		}
		if((access & WALK_IN) > 0) {
			if(buffer.length() > 0) buffer.append(", ");
			buffer.append("Walk-in");
		}
		if((access & OTHER) > 0) {
			if(buffer.length() > 0) buffer.append(", ");
			buffer.append("Other");
		}
		if(buffer.length() == 0) buffer.append("None");
		return buffer.toString();
	}
}
