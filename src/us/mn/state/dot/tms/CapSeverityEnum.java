/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
 * Common Alerting Protocol (CAP) severity field value enum. Used for IPAWS
 * alert processing for generating messages for posting to DMS. Values are
 * taken from the OASIS CAP Standard v1.2. Values are ordered from least
 * (Unknown/Minor) to most (Extreme) emphatic for use in calculating message
 * priority.
 *
 * @author Gordon Parikh
 */
public enum CapSeverityEnum {
	UNKNOWN("Unknown", "Severity unknown"),
	MINOR("Minor", "Minimal to no known threat to life or property"),
	MODERATE("Moderate", "Possible threat to life or property"),
	SEVERE("Severe", "Significant threat to life or property"),
	EXTREME("Extreme", "Extraordinary threat to life or property");

	/** Value used in CAP messages */
	public final String value;
	
	/** Description of value from CAP standard */
	public final String description;
	
	private CapSeverityEnum(String v, String d) {
		value = v;
		description = d;
	}
	
	/** Return the CapSeverityEnum from the value provided. */
	static public CapSeverityEnum fromValue(String v) {
		for (CapSeverityEnum e: values()) {
			if (e.value.equals(v))
				return e;
		}
		return UNKNOWN;
	}
	
	/** Return an array of the string values (i.e. the ones seen in a CAP
	 *  message).
	 */
	static public String[] stringValues() {
		CapSeverityEnum[] evals = values();
		String[] svals = new String[evals.length];
		for (int i = 0; i < evals.length; ++i)
			svals[i] = evals[i].value;
		return svals;
	}

	/** Return the number of possible values. */
	static public int nValues() {
		return values().length;
	}
}
