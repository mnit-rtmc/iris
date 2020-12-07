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
 * Common Alerting Protocol (CAP) response type field value enum. Used for
 * IPAWS alert processing for generating messages for posting to DMS. Values
 * are taken from the OASIS CAP Standard v1.2. 
 *
 * @author Gordon Parikh
 */
public enum CapResponseTypeEnum {
	NONE("None", "No action recommended"),
	ALL_CLEAR("All Clear", "The subject event no longer poses a threat or " +
			"concern and any follow on action is described in <instruction>"),
	MONITOR("Monitor",
			"Attend to information sources as described in <instruction>"),
	AVOID("Avoid", "Avoid the subject event as per the <instruction>"),
	EXECUTE("Execute",
			"Execute a pre-planned activity identified in <instruction>"),
	PREPARE("Prepare", "Make preparations per the <instruction>"),
	EVACUATE("Evacuate", "Relocate as instructed in the <instruction>"),
	SHELTER("Shelter", "Take shelter in place or per <instruction>");
	
	// NOTE the following is included in the CAP standard but is advised to
	// not be used for public warning applications and should not be enabled
	// here
	// ASSESS("Assess", "Evaluate the information in this message. (This " +
	// 	"value SHOULD NOT be used in public warning applications.)");
	
	/** Value used in CAP messages */
	public final String value;

	/** Description of value from CAP standard */
	public final String description;
	
	private CapResponseTypeEnum(String v, String d) {
		value = v;
		description = d;
	}
	
	/** Return the CapResponseTypeEnum from the value provided. */
	static public CapResponseTypeEnum fromValue(String v) {
		for (CapResponseTypeEnum e: values()) {
			if (e.value.equals(v))
				return e;
		}
		return NONE;
	}
	
	/** Return an array of the string values (i.e. the ones seen in a CAP
	 *  message).
	 */
	static public String[] stringValues() {
		CapResponseTypeEnum[] evals = values();
		String[] svals = new String[evals.length];
		for (int i = 0; i < evals.length; ++i)
			svals[i] = evals[i].value;
		return svals;
	}
}
