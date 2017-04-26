/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
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

/**
 * Device request enumeration.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public enum DeviceRequest {

	/** No request */
	NO_REQUEST,

	/** Query device configuration */
	QUERY_CONFIGURATION,

	/** Send device settings */
	SEND_SETTINGS,

	/** Query current message */
	QUERY_MESSAGE,

	/** Query device status */
	QUERY_STATUS,

	/** Query sign pixel failures */
	QUERY_PIXEL_FAILURES,

	/** Activate pixel test */
	TEST_PIXELS,

	/** Activate fan test */
	TEST_FANS,

	/** Activate lamp test */
	TEST_LAMPS,

	/** Sign brightness feedback "good" */
	BRIGHTNESS_GOOD,

	/** Sign brightness feedback "too dim" */
	BRIGHTNESS_TOO_DIM,

	/** Sign brightness feedback "too bright" */
	BRIGHTNESS_TOO_BRIGHT,

	/** Reset device */
	RESET_DEVICE,

	/** Reset modem */
	RESET_MODEM,

	/** Send LEDSTAR sign settings */
	SEND_LEDSTAR_SETTINGS,

	/** Query LEDSTAR sign settings */
	QUERY_LEDSTAR_SETTINGS,

	/** Disable (gate arm) system */
	DISABLE_SYSTEM,

	/** CAMERA: stop focus */
	CAMERA_FOCUS_STOP,

	/** CAMERA: focus near */
	CAMERA_FOCUS_NEAR,

	/** CAMERA: focus far */
	CAMERA_FOCUS_FAR,

	/** CAMERA: manual-focus */
	CAMERA_FOCUS_MANUAL,

	/** CAMERA: auto-focus */
	CAMERA_FOCUS_AUTO,

	/** CAMERA: stop iris */
	CAMERA_IRIS_STOP,

	/** CAMERA: close iris */
	CAMERA_IRIS_CLOSE,

	/** CAMERA: open iris */
	CAMERA_IRIS_OPEN,

	/** CAMERA: manual-iris */
	CAMERA_IRIS_MANUAL,

	/** CAMERA: auto-iris */
	CAMERA_IRIS_AUTO,

	/** CAMERA: wiper one-shot */
	CAMERA_WIPER_ONESHOT;

	/** Cached values array */
	static private final DeviceRequest[] VALUES = values();

	/** Get device request from an ordinal value */
	static public DeviceRequest fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : NO_REQUEST;
	}
}
