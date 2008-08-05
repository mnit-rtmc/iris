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

import us.mn.state.dot.tms.DmsSignMatrixType;

/**
 * Ntcip DmsSignType object
 *
 * @author Douglas Lau
 */
public class DmsSignType extends DmsSignCfg implements ASN1Integer {

	/** Sign types */
	static protected final String[] TYPE = { "?", "Other", "BOS", "CMS",
		"VMS Character", "VMS Line", "VMS Full", "??" };

	/** Create a new DmsSignType object */
	public DmsSignType() {
		super(2);
	}

	/** Get the object name */
	protected String getName() { return "dmsSignType"; }

	/** Sign type */
	protected int type;

	/** Set the integer value */
	public void setInteger(int value) { type = value; }

	/** Get the integer value */
	public int getInteger() { return type; }

	/** Get the object value as a String */
	public String getValue() {
		StringBuffer buffer = new StringBuffer();
		if(( type & 0x80) != 0) buffer.append("Portable ");
		buffer.append(TYPE[type & 0x07]);
		return buffer.toString();
	}

	/** Get the object value as a DmsSignMatrixType */
	public DmsSignMatrixType getValueEnum() {
//FIXME: hi Doug, I'm guessing here...
		String v=(getValue()==null ? "" : getValue().toLowerCase());
		if (v.contains("character"))
			return DmsSignMatrixType.CHARACTER;
		else if (v.contains("line"))
			return DmsSignMatrixType.LINE;
		else if (v.contains("cms"))
			return DmsSignMatrixType.FULL;
		System.err.println("WARNING: unknown ntcip sign type encountered: "+v);
		return DmsSignMatrixType.FULL;
	}

}
