/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.snmp;

/**
 * ASN1 Bit Flags Enum.
 *
 * @author Douglas Lau
 */
public class ASN1Flags<T extends Enum> extends ASN1Enum {

	/** Create a new ASN1 flags enum.
	 * @param n MIB node.
	 * @param idx Node index.
	 * @param j Table index. */
	public ASN1Flags(Class<T> e, MIBNode n, int idx, int j) {
		super(e, n, idx, j);
	}

	/** Create a new ASN1 flags enum.
	 * @param n MIB node.
	 * @param idx Node index. */
	public ASN1Flags(Class<T> e, MIBNode n, int idx) {
		super(e, n, idx);
	}

	/** Create a new ASN1 flags enum.
	 * @param n MIB node. */
	public ASN1Flags(Class<T> e, MIBNode n) {
		super(e, n);
	}

	/** Get the object value */
	@Override
	public String getValue() {
		StringBuilder buf = new StringBuilder();
		Enum[] values = lookupEnumConstants();
		if (values != null) {
			int v = getInteger();
			for (int i = 0; i < values.length; i++) {
				int bit = 1 << i;
				if ((v & bit) != 0) {
					if (buf.length() > 0)
						buf.append(", ");
					buf.append(values[i]);
				}
			}
		}
		return buf.toString();
	}
}
