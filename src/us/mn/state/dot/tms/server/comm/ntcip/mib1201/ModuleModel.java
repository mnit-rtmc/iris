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
package us.mn.state.dot.tms.server.comm.ntcip.mib1201;

import us.mn.state.dot.tms.server.comm.ntcip.ASN1OctetString;

/**
 * Ntcip ModuleModel object
 *
 * @author Douglas Lau
 */
public class ModuleModel extends ASN1OctetString {

	/** Create a new module model object */
	public ModuleModel(int row) {
		super(MIB1201.moduleTableEntry.create(new int[] { 4, row }));
	}

	/** Get the object value */
	public String getValue() {
		return new String(value);
	}
}
