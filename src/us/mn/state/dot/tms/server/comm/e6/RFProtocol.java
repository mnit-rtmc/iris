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
package us.mn.state.dot.tms.server.comm.e6;

import java.util.LinkedList;

/**
 * RF protocol definitions for E6 multiprotocol reader.
 *
 * @author Douglas Lau
 */
public enum RFProtocol {
	IT2200		(4),	/* 0 */
	SeGo		(6),	/* 1 */
	IAG		(7),	/* 2 */
	ASTMv6		(9),	/* 3 (aka CVISN) */
	Title21		(3),	/* 4 */
	ATA_Full	(1),	/* 5 */
	eGo		(5),	/* 6 */
	ATA_Half	(2);	/* 7 */

	/** Create a new RF protocol */
	private RFProtocol(int b) {
		bit = 1 << b;
	}

	/** Bit assignment */
	public final int bit;

	/** Lookup an RF protocol from an ordinal value */
	static public RFProtocol fromOrdinal(int o) {
		for (RFProtocol p: values()) {
			if (p.ordinal() == o)
				return p;
		}
		return null;
	}

	/** Get the protocols from a set of bits */
	static public RFProtocol[] fromBits(int b) {
		LinkedList<RFProtocol> list = new LinkedList<RFProtocol>();
		for (RFProtocol p: values()) {
			if ((b & p.bit) != 0)
				list.add(p);
		}
		return list.toArray(new RFProtocol[0]);
	}
}
