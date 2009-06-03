/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2009  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;

/**
 * Ntcip DefaultJustificationPage object
 *
 * @author Douglas Lau
 */
public class DefaultJustificationPage extends ASN1Integer {

	/** Create a new DefaultJustificationPage object */
	public DefaultJustificationPage() {
		super(MIB1203.multiCfg.create(new int[] {7, 0}));
	}

	/** Set the enum value */
	public void setEnum(MultiString.JustificationPage j) {
		value = j.ordinal();
	}

	/** Get the object value */
	public String getValue() {
		return MultiString.JustificationPage.fromOrdinal(
		       value).toString();
	}
}
