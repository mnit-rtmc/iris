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
package us.mn.state.dot.tms.server.comm.ntcip;

/**
 * Ntcip ModuleMake object
 *
 * @author Douglas Lau
 */
public class ModuleMake extends GlobalModuleTable implements ASN1OctetString {

	/** Create a new ModuleMake object */
	public ModuleMake(int i) {
		super(i);
	}

	/** Get the object name */
	protected String getName() { return "moduleMake"; }

	/** Get the module table item (for moduleMake objects) */
	protected int getTableItem() { return 3; }

	/** Actual module make */
	protected String make;

	/** Set the octet string value */
	public void setOctetString(byte[] value) { make = new String(value); }

	/** Get the octet string value */
	public byte[] getOctetString() { return make.getBytes(); }

	/** Get the object value */
	public String getValue() { return make; }
}
