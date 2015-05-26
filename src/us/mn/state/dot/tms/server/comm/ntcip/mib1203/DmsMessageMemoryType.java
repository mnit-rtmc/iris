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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

/**
 * Enumeration of memory types.
 *
 * @author Douglas Lau
 */
public enum DmsMessageMemoryType {
	undefined	(false),
	other		(false),
	permanent	(true),
	changeable	(true),
	_volatile	(true),
	currentBuffer	(false),
	schedule	(true),
	blank		(true);

	/** Valid for deploying messages */
	public final boolean valid;

	/** Create a new message memory type */
	private DmsMessageMemoryType(boolean v) {
		valid = v;
	}

	/** Test if a message memory type is "blank" */
	public boolean isBlank() {
	 	// For some vendors (1203v1), blank messages are
	 	// undefined in dmsMsgTableSource
		return this == blank || this == undefined;
	}

	/** Get memory type from an ordinal value */
	static public DmsMessageMemoryType fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return undefined;
	}
}
