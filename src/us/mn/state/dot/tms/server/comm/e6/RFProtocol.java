/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2023  Minnesota Department of Transportation
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
	@Deprecated
	IT2200,   /* 0 (legacy, unsupported) */
	SeGo,     /* 1 (TransCore Super eGo) */
	IAG,      /* 2 (E-Zpass InterAgency Group) */
	@Deprecated
	ASTMv6,   /* 3 (legacy, unsupported) */
	@Deprecated
	Title21,  /* 4 (legacy, unsupported) */
	@Deprecated
	ATA_Full, /* 5 (legacy, unsupported) */
	@Deprecated
	eGo,      /* 6 (legacy, unsupported) */
	@Deprecated
	ATA_Half, /* 7 (legacy, unsupported) */
	_6C;      /* 8 (ISO/IEC 18000-63) */

	/** Lookup an RF protocol from an ordinal value */
	static public RFProtocol fromOrdinal(int o) {
		for (RFProtocol p: values()) {
			if (p.ordinal() == o)
				return p;
		}
		return null;
	}
}
