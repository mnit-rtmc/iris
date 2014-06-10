/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
 * Copyright (C) 2014  AHMCT, University of California
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
 * @author Travis Swanston
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
	QUERY_LEDSTAR_SETTINGS("Query LEDSTAR settings"),

	/** Disable (gate arm) system */
	DISABLE_SYSTEM("Disable system"),

	/** CAMERA: stop focus */
	CAMERA_FOCUS_STOP("Camera: stop focus"),

	/** CAMERA: focus near */
	CAMERA_FOCUS_NEAR("Camera: focus near"),

	/** CAMERA: focus far */
	CAMERA_FOCUS_FAR("Camera: focus far"),

	/** CAMERA: manual-focus */
	CAMERA_FOCUS_MANUAL("Camera: manual-focus"),

	/** CAMERA: auto-focus */
	CAMERA_FOCUS_AUTO("Camera: auto-focus"),

	/** CAMERA: auto-focus toggle */
	CAMERA_FOCUS_TOGGLE("Camera: auto-focus toggle"),

	/** CAMERA: stop iris */
	CAMERA_IRIS_STOP("Camera: stop iris"),

	/** CAMERA: close iris */
	CAMERA_IRIS_CLOSE("Camera: close iris"),

	/** CAMERA: open iris */
	CAMERA_IRIS_OPEN("Camera: open iris"),

	/** CAMERA: manual-iris */
	CAMERA_IRIS_MANUAL("Camera: manual-iris"),

	/** CAMERA: auto-iris */
	CAMERA_IRIS_AUTO("Camera: auto-iris"),

	/** CAMERA: auto-iris toggle */
	CAMERA_IRIS_TOGGLE("Camera: auto-iris toggle"),

	/** CAMERA: wiper on */
	CAMERA_WIPER_ON("Camera: wiper on"),

	/** CAMERA: wiper off */
	CAMERA_WIPER_OFF("Camera: wiper off"),

	/** CAMERA: wiper toggle */
	CAMERA_WIPER_TOGGLE("Camera: wiper toggle"),

	/** CAMERA: wiper one-shot */
	CAMERA_WIPER_ONESHOT("Camera: wiper one-shot");


	/** Create a new device request value */
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
