/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
 * Copyright (C) 2015  Iteris Inc.
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
 * SNMP Identifier tag.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public enum SNMPTag implements Tag {
	GET_REQUEST		(CONTEXT, true, 0),
	GET_NEXT_REQUEST	(CONTEXT, true, 1),
	GET_RESPONSE		(CONTEXT, true, 2),
	SET_REQUEST		(CONTEXT, true, 3),
	TRAP			(CONTEXT, true, 4),
	COUNTER			(APPLICATION, false, 1),
	INTEGER_SKYLINE		(APPLICATION, false, 2);

	/** Tag class */
	private final byte clazz;

	/** Get the tag class */
	@Override
	public byte getClazz() {
		return clazz;
	}

	/** Constructed tag flag */
	private final boolean constructed;

	/** Is the tag constructed? */
	@Override
	public boolean isConstructed() {
		return constructed;
	}

	/** Tag number */
	private final int number;

	/** Get tag number */
	@Override
	public int getNumber() {
		return number;
	}

	/** Create a new SNMP identifier tag */
	private SNMPTag(byte c, boolean co, int n) {
		clazz = c;
		constructed = co;
		number = n;
	}

	/** Lookup an SNMP tag from values */
	static public SNMPTag fromValues(byte c, boolean co, int n) {
		for (SNMPTag t: values()) {
			if (t.clazz == c && t.constructed == co && t.number ==n)
				return t;
		}
		return null;
	}
}
