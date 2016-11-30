/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;

/**
 * Ntcip DmsSignType object
 *
 * @author Douglas Lau
 */
public class DmsSignType extends ASN1Integer {

	/** Create a new DmsSignType object */
	public DmsSignType() {
		super(MIB1203.dmsSignType.node);
	}

	/** Get the object value as a String */
	@Override
	public String getValue() {
		StringBuilder b = new StringBuilder();
		if (isPortable())
			b.append("Portable ");
		b.append(getValueEnum().description);
		return b.toString();
	}

	/** Get the object value as a DMSType */
	public DMSType getValueEnum() {
		return DMSType.fromOrdinal(getInteger() & 0x7f);
	}

	/** Is the type portable? */
	public boolean isPortable() {
		return (getInteger() & 0x80) != 0;
	}
}
