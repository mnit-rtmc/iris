/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2016  Minnesota Department of Transportation
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
 * Modem state enumeration.
 *
 * @author Douglas Lau
 */
public enum ModemState {

	/** Modem states */
	offline, connecting, online, open_error, connect_error;

	/** Get a modem state from an ordinal value */
	static public ModemState fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return null;
	}

	/** Check if a modem state is in error */
	static public boolean isError(int o) {
		return (o == open_error.ordinal()) ||
		       (o == connect_error.ordinal());
	}
}
