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
 * Precipitation situation as defined by essPrecipSituation in NTCIP 1204.
 *
 * @author Michael Darter
 */
public enum PrecipSituation {

	/** Precipitation situation values defined by essPrecipSituation */
	UNDEFINED("???"),
	OTHER("Other"),
	UNKNOWN("Unknown"),
	NO_PRECIPITATION("noPrecipitation"),
	UNIDENTIFIED_SLIGHT("unidentifiedSlight"),
	UNIDENTIFIED_MODERATE("unidentifiedModerate"),
	UNIDENTIFIED_HEAVY("unidentifiedHeavy"),
	SNOW_SLIGHT("snowSlight"),
	SNOW_MODERATE("snowModerate"),
	SNOW_HEAVY("snowHeavy"),
	RAIN_SLIGHT("rainSlight"),
	RAIN_MODERATE("rainModerate"),
	RAIN_HEAVY("rainHeavy"),
	FROZEN_PRECIPITATION_SLIGHT("frozenPrecipitationSlight"),
	FROZEN_PRECIPITATION_MODERATE("frozenPrecipitationModerate"),
	FROZEN_PRECIPITATION_HEAVY("frozenPrecipitationHeavy");

	/** Description string */
	public final String description;

	/** Constructor */
	private PrecipSituation(String d) {
		description = d;
	}

	/** Get an enum from an ordinal value */
	static public PrecipSituation fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return UNDEFINED;
	}
}
