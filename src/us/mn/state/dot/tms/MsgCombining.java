/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 * DMS message combining enumeration.  The ordinal values correspond to the
 * records in the iris.msg_combining look-up table.
 *
 * @author Douglas Lau
 */
public enum MsgCombining {
	DISABLE, /* 0: may not be combined */
	FIRST,   /* 1: may combine as first message */
	SECOND,  /* 2: may combine as second message */
	EITHER;  /* 3: may combine as first or second message */

	/** Values array */
	static private final MsgCombining[] VALUES = values();

	/** Get a MsgCombining from an ordinal value */
	static public MsgCombining fromOrdinal(int o) {
		if (o >= 0 && o < VALUES.length)
			return VALUES[o];
		else
			return DISABLE;
	}
}
