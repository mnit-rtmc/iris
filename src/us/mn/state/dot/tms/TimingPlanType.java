/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
 * Timing plan type enumeration.
 *
 * @author Douglas Lau
 */
public enum TimingPlanType {

	/** Travel time timing plan */
	TRAVEL("Travel Time"),

	/** Simple metering plan */
	SIMPLE("Simple Metering"),

	/** Stratified metering plan */
	STRATIFIED("Stratified Metering");

	/** Create a new timing plan type */
	private TimingPlanType(String d) {
		description = d;
	}

	/** Description of the type */
	public final String description;

	/** Get a timing plan type from an ordinal value */
	static public TimingPlanType fromOrdinal(int o) {
		for(TimingPlanType t: TimingPlanType.values()) {
			if(t.ordinal() == o)
				return t;
		}
		return TRAVEL;
	}

	/** Get an array of timing plan type descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(TimingPlanType t: TimingPlanType.values())
			d.add(t.description);
		return d.toArray(new String[0]);
	}
}
