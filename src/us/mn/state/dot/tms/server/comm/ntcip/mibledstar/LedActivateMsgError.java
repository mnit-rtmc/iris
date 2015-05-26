/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mibledstar;

import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;

/**
 * Ledstar LedActivateMsgError object
 *
 * @author Douglas Lau
 */
public class LedActivateMsgError extends ASN1Integer {

	/** Activate message error descriptions */
	static private final String[] ERROR = {
		"Over temperature", "Bad pixel limit", "Draw error"
	};

	/** Create a new LedActivateMsgError */
	public LedActivateMsgError() {
		super(MIB.ledActivateMsgError.node);
	}

	/** Get the object value */
	@Override
	public String getValue() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < ERROR.length; i++) {
			int bit = 1 << i;
			if ((value & bit) != 0) {
				if (b.length() > 0)
					b.append(" / ");
				b.append(ERROR[i]);
			}
		}
		if (b.length() < 1)
			b.append("None");
		return b.toString();
	}
}
