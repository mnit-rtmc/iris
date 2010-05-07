/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.ntcip.ASN1OctetString;

/**
 * The DmsPowerFailureStatusMap object contains a bitmap of failed power
 * supplies within the sign.  This object was added in NTCIP 1203 v2.
 *
 * @author Douglas Lau
 */
public class DmsPowerFailureStatusMap extends ASN1OctetString {

	/** Create a new DmsPowerFailureStatusMap object */
	public DmsPowerFailureStatusMap() {
		super(MIB1203.statError.create(new int[] {11, 0}));
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder sb = new StringBuilder();
		int p = 1;
		for(byte v: value) {
			for(int bit = 0x01; bit < 0x0100; bit <<= 1, p++) {
				if((v & bit) != 0) {
					sb.append(", #");
					sb.append(p);
				}
			}
		}
		if(sb.length() < 2)
			return "OK";
		else
			return sb.substring(2);
	}
}
