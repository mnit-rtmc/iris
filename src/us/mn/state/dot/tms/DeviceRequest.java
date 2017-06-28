/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
 * Copyright (C) 2014       AHMCT, University of California
 * Copyright (C) 2015-2017  SRF Consulting Group
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
 * @author John L. Stanley - SRF Consulting
 */
public enum DeviceRequest {
	NO_REQUEST,
	QUERY_CONFIGURATION,
	SEND_SETTINGS,
	QUERY_MESSAGE,
	QUERY_STATUS,
	QUERY_PIXEL_FAILURES,
	QUERY_GPS_LOCATION,
	QUERY_GPS_LOCATION_FORCE,
	TEST_PIXELS,
	TEST_FANS,
	TEST_LAMPS,
	BRIGHTNESS_GOOD,
	BRIGHTNESS_TOO_DIM,
	BRIGHTNESS_TOO_BRIGHT,
	RESET_DEVICE,
	RESET_MODEM,
	SEND_LEDSTAR_SETTINGS,
	QUERY_LEDSTAR_SETTINGS,
	DISABLE_SYSTEM,
	CAMERA_FOCUS_STOP,
	CAMERA_FOCUS_NEAR,
	CAMERA_FOCUS_FAR,
	CAMERA_FOCUS_MANUAL,
	CAMERA_FOCUS_AUTO,
	CAMERA_IRIS_STOP,
	CAMERA_IRIS_CLOSE,
	CAMERA_IRIS_OPEN,
	CAMERA_IRIS_MANUAL,
	CAMERA_IRIS_AUTO,
	CAMERA_WIPER_ONESHOT,
	CAMERA_WASHER,
	CAMERA_POWER_ON,
	CAMERA_POWER_OFF,
	CAMERA_MENU_OPEN,
	CAMERA_MENU_ENTER,
	CAMERA_MENU_CANCEL;

	/** Cached values array */
	static private final DeviceRequest[] VALUES = values();

	/** Get device request from an ordinal value */
	static public DeviceRequest fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : NO_REQUEST;
	}
}
