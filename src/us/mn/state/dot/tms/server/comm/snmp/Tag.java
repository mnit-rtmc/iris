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
 * Tag interface.
 *
 * @author Douglas Lau
 */
public interface Tag {

	/** Universal tag class */
	byte UNIVERSAL = 0x00;

	/** Application tag class */
	byte APPLICATION = 0x40;

	/** Context-specific tag class */
	byte CONTEXT = (byte)0x80;

	/** Private tag class */
	byte PRIVATE = (byte)0xC0;

	/** Tag class mask */
	byte CLASS_MASK = (byte)0xC0;

	/** Identifier bit mask for constructed encoding */
	byte CONSTRUCTED = 0x20;

	/** Get tag class */
	byte getClazz();

	/** Is the tag constructed? */
	boolean isConstructed();

	/** Get tag number */
	int getNumber();
}
