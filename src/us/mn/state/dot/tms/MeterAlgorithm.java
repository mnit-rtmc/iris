/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
 * Meter algorithm enumeration.
 *
 * @author Douglas Lau
 */
public enum MeterAlgorithm {

	/** No metering */
	NONE("No metering"),

	/** Simple metering algorithm */
	SIMPLE("Simple Metering"),

	/** Stratified metering algorithm */
	STRATIFIED("SZM (obsolete)"),

	/** Density (K) Adaptive metering algorithm */
	K_ADAPTIVE("K Adaptive Metering");

	/** Create a new metering algorithm */
	private MeterAlgorithm(String d) {
		description = d;
	}

	/** Description of the algorithm */
	public final String description;

	/** Get the string representation */
	@Override
	public String toString() {
		return description;
	}

	/** Get a meter algorithm from an ordinal value */
	static public MeterAlgorithm fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return NONE;
	}
}
