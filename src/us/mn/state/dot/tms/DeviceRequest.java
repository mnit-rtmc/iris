/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
 * Device request enumeration.
 *
 * @author Douglas Lau
 */
public enum DeviceRequest {

	/** No request */
	NO_REQUEST(""),

	/** Query device configuration */
	QUERY_CONFIGURATION("Query configuration"),

	/** Send device settings */
	SEND_SETTINGS("Send settings"),

	/** Query current message */
	QUERY_MESSAGE("Query message"),

	/** Query device status */
	QUERY_STATUS("Query status"),

	/** Query sign pixel failures */
	QUERY_PIXEL_FAILURES("Query pixel failures"),

	/** Activate pixel test */
	TEST_PIXELS("Pixel test"),

	/** Activate fan test */
	TEST_FANS("Fan test"),

	/** Activate lamp test */
	TEST_LAMPS("Lamp test"),

	/** Sign brightness feedback "good" */
	BRIGHTNESS_GOOD("Brightness good"),

	/** Sign brightness feedback "too dim" */
	BRIGHTNESS_TOO_DIM("Brightness too dim"),

	/** Sign brightness feedback "too bright" */
	BRIGHTNESS_TOO_BRIGHT("Brightness too bright"),

	/** Reset device */
	RESET_DEVICE("Reset Device"),

	/** Reset modem */
	RESET_MODEM("Reset modem"),

	/** Send LEDSTAR sign settings */
	SEND_LEDSTAR_SETTINGS("Send LEDSTAR settings"),

	/** Query LEDSTAR sign settings */
	QUERY_LEDSTAR_SETTINGS("Query LEDSTAR settings");

	/** Create a new deivce request value */
	private DeviceRequest(String d) {
		description = d;
	}

	/** Description */
	public final String description;

	/** Get device request from an ordinal value */
	static public DeviceRequest fromOrdinal(int o) {
		for(DeviceRequest dr: DeviceRequest.values()) {
			if(dr.ordinal() == o)
				return dr;
		}
		return NO_REQUEST;
	}

	/** Get an array of device request descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(DeviceRequest dr: DeviceRequest.values())
			d.add(dr.description);
		return d.toArray(new String[0]);
	}
}
