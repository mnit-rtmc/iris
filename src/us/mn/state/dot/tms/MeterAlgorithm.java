/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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

import java.util.LinkedList;

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
	STRATIFIED("Stratified Metering"),

	/** Density (K) Adaptive metering algorithm */
	K_ADAPTIVE("K Adaptive Metering");

	/** Create a new metering algorithm */
	private MeterAlgorithm(String d) {
		description = d;
	}

	/** Description of the algorithm */
	public final String description;

	/** Get a meter algorithm from an ordinal value */
	static public MeterAlgorithm fromOrdinal(int o) {
		for(MeterAlgorithm ma: MeterAlgorithm.values()) {
			if(ma.ordinal() == o)
				return ma;
		}
		return NONE;
	}

	/** Get an array of meter algorithm descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(MeterAlgorithm ma: MeterAlgorithm.values())
			d.add(ma.description);
		return d.toArray(new String[0]);
	}
}
