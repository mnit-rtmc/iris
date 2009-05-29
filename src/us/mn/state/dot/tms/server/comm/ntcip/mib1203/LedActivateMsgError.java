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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Int;

/**
 * Ledstar LedActivateMsgError object
 *
 * @author Douglas Lau
 */
public class LedActivateMsgError extends ASN1Int {

	/** Activate message error descriptions */
	static protected final String[] ERROR = {
		"Over temperature", "Bad pixel limit", "Draw error"
	};

	/** Bit masks */
	static protected final int[] BIT = { 1, 2, 4 };

	/** Get the object identifier */
	public int[] getOID() {
		return MIBNode.ledstarDiagnostics.createOID(new int[] {12, 0});
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < 3; i++) {
			if((value & BIT[i]) != 0) {
				if(b.length() > 0)
					b.append(" / ");
				b.append(ERROR[i]);
			}
		}
		if(b.length() < 1)
			b.append("None");
		return b.toString();
	}
}
