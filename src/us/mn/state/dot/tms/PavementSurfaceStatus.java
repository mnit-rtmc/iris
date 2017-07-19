/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Iteris Inc.
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
 * Pavement surface status as defined by essSurfaceStatus in NTCIP 1204.
 *
 * @author Michael Darter
 */
public enum PavementSurfaceStatus {

	/** Pavement surface status values defined by essSurfaceStatus */
	UNDEFINED("???"),
	OTHER("Other"),	
	ERROR("Error"),
	DRY("Dry"),
	TRACE_MOISTURE("traceMoisture"),
	WET("Wet"),
	CHEMICALLY_WET("chemicallyWet"),
	ICE_WARNING("iceWarning"),
	ICE_WATCH("iceWatch"),
	SNOW_WARNING("snowWarning"),
	SNOW_WATCH("snowWatch"),
	ABSORPTION("Absorption"),
	DEW("Dew"),
	FROST("Frost"),
	ABSORPTION_AT_DEWPOINT("absorptionAtDewpoint");

	/** Description string */
	public final String description;

	/** Constructor */
	private PavementSurfaceStatus(String d) {
		description = d;
	}

	/** Get an enum from an ordinal value */
	static public PavementSurfaceStatus fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return UNDEFINED;
	}
}
