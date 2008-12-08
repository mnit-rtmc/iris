/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
 * Sign request enumeration.
 *
 * @author Douglas Lau
 */
public enum SignRequest {

	/** No request */
	NO_REQUEST(""),

	/** Query sign configuration */
	QUERY_CONFIGURATION("Query configuration"),

	/** Query current sign message */
	QUERY_MESSAGE("Query message"),

	/** Query sign status */
	QUERY_STATUS("Query status"),

	/** Query pixel failures */
	QUERY_PIXEL_FAILURES("Query pixel failures"),

	/** Activate pixel test */
	TEST_PIXELS("Pixel test"),

	/** Activate fan test */
	TEST_FANS("Fan test"),

	/** Activate lamp test */
	TEST_LAMPS("Lamp test"),

	/** Brightness feedback "good" */
	BRIGHTNESS_GOOD("Brightness good"),

	/** Brightness feedback "too dim" */
	BRIGHTNESS_TOO_DIM("Brightness too dim"),

	/** Brightness feedback "too bright" */
	BRIGHTNESS_TOO_BRIGHT("Brightness too bright"),

	/** Reset DMS */
	RESET_DMS("Reset DMS"),

	/** Reset modem */
	RESET_MODEM("Reset modem"),

	/** Send LEDSTAR settings */
	SEND_LEDSTAR_SETTINGS("Send LEDSTAR settings"),

	/** Query LEDSTAR settings */
	QUERY_LEDSTAR_SETTINGS("Query LEDSTAR settings");

	/** Create a new sign request value */
	private SignRequest(String d) {
		description = d;
	}

	/** Description */
	public final String description;

	/** Get sign request from an ordinal value */
	static public SignRequest fromOrdinal(int o) {
		for(SignRequest sr: SignRequest.values()) {
			if(sr.ordinal() == o)
				return sr;
		}
		return NO_REQUEST;
	}

	/** Get an array of sign request descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(SignRequest sr: SignRequest.values())
			d.add(sr.description);
		return d.toArray(new String[0]);
	}
}
