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
 * LedLdcPotBase object
 *
 * @author Douglas Lau
 */
public class LedLdcPotBase extends LedstarSignControl
	implements ASN1Integer
{
	/** Create a new LedLdcPotBase object */
	public LedLdcPotBase() {
		this(0);
	}

	/** Create a new LedLdcPotBase object */
	public LedLdcPotBase(int b) {
		super(6);
		base = b;
	}

	/** Get the object name */
	protected String getName() { return "LedLdcPotBase"; }

	/** Ldc pot base */
	protected int base;

	/** Set the integer value */
	public void setInteger(int value) { base = value; }

	/** Get the integer value */
	public int getInteger() { return base; }

	/** Get the object value */
	public String getValue() { return String.valueOf(base); }
}
