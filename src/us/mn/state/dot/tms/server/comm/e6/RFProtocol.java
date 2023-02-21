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
	SeGo(1),  /* TransCore Super eGo */
	IAG(2),   /* E-Zpass InterAgency Group */
	_6C(8);   /* ISO/IEC 18000-63 */

	/** Protocol value for E6 */
	public final int value;

	/** Create an RF protocol */
	private RFProtocol(int v) {
		value = v;
	}

	/** Lookup an RF protocol from a value */
	static public RFProtocol fromValue(int p) {
		switch (p) {
			case 1: return SeGo;
			case 2: return IAG;
			case 8: return _6C;
			default: return null;
		}
	}

	/** Get the next protocol */
	static public RFProtocol next(RFProtocol p) {
		if (null == p)
			return SeGo;
		switch (p) {
			case SeGo: return IAG;
			case IAG: return _6C;
			default: return null;
		}
	}
}
