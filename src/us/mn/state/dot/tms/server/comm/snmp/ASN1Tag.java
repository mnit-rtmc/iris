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
package us.mn.state.dot.tms.server.comm.snmp;

/**
 * Enum for ASN1 tags.
 *
 * @author Douglas Lau
 */
public enum ASN1Tag implements Tag {
	BOOLEAN			(UNIVERSAL, false, 1),
	INTEGER			(UNIVERSAL, false, 2),
	BIT_STRING		(UNIVERSAL, false, 3),
	OCTET_STRING		(UNIVERSAL, false, 4),
	NULL			(UNIVERSAL, false, 5),
	OBJECT_IDENTIFIER	(UNIVERSAL, false, 6),
	SEQUENCE		(UNIVERSAL, true, 16);

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

	/** Create a new ASN1 identifier tag */
	private ASN1Tag(byte c, boolean co, int n) {
		clazz = c;
		constructed = co;
		number = n;
	}

	/** Lookup an ASN1 tag from values */
	static public ASN1Tag fromValues(byte c, boolean co, int n) {
		for (ASN1Tag t: values()) {
			if (t.clazz == c && t.constructed == co && t.number ==n)
				return t;
		}
		return null;
	}
}
