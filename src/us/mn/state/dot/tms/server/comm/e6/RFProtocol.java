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

/**
 * RF protocol definitions for E6 multiprotocol reader.
 *
 * @author Douglas Lau
 */
public enum RFProtocol {
	IT2200,		/* 0 */
	SeGo,		/* 1 */
	IAG,		/* 2 */
	ASTMv6,		/* 3 (aka CVISN) */
	Title21,	/* 4 */
	ATA_Full,	/* 5 */
	eGo,		/* 6 */
	ATA_Half;	/* 7 */

	/** Lookup an RF protocol from an ordinal value. */
	static public RFProtocol fromOrdinal(int o) {
		for (RFProtocol p: values()) {
			if (p.ordinal() == 0)
				return p;
		}
		return null;
	}
}
