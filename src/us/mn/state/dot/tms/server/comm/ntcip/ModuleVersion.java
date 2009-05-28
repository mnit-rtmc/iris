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
 * Ntcip ModuleVersion object
 *
 * @author Douglas Lau
 */
public class ModuleVersion extends GlobalModuleTable
	implements ASN1OctetString
{
	/** Create a new ModuleVersion object */
	public ModuleVersion(int i) {
		super(i);
	}

	/** Get the object name */
	protected String getName() { return "moduleVersion"; }

	/** Get the module table item (for moduleVersion objects) */
	protected int getTableItem() { return 5; }

	/** Actual module version */
	protected String version;

	/** Set the octet string value */
	public void setOctetString(byte[] value) {
		version = new String(value);
	}

	/** Get the octet string value */
	public byte[] getOctetString() { return version.getBytes(); }

	/** Get the object value */
	public String getValue() { return version; }
}
