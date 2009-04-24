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

/**
 * This enum defines all system attributes.
 *
 * @author Douglas Lau
 */
public enum SystemAttrEnum {
	CAMERA_NUM_PRESET_BTNS(3, 0, 20),
	CAMERA_NUM_VIDEO_FRAMES(900, 0),
	CAMERA_PTZ_PANEL_ENABLE(false),
	DATABASE_VERSION(String.class),
	DMS_PAGE_ON_SECS(2f, 0f, 60f),
	DMS_PAGE_OFF_SECS(0f, 0f, 60f),
	DMS_POLL_FREQ_SECS(30, 5),
	DMS_MAX_LINES(3, 1, 12),
	DMS_PIXEL_TEST_TIMEOUT_SECS(30, 5, 90),
	DMS_LAMP_TEST_TIMEOUT(30, 5, 90),
	DMS_PIXEL_OFF_LIMIT(2, 1),
	DMS_PIXEL_ON_LIMIT(1, 1),
	DMS_HIGH_TEMP_CUTOFF(60, 35, 100),
	DMS_DEFAULT_JUSTIFICATION_LINE(3, 2, 5),
	DMS_DEFAULT_JUSTIFICATION_PAGE(2, 2, 4),
	DMS_MESSAGE_MIN_PAGES(1, 1, 6),
	DMS_MESSAGE_BLANK_LINE_ENABLE(true),
	DMS_STATUS_ENABLE(false),
	DMS_AWS_ENABLE(false),
	DMS_DURATION_ENABLE(true),
	DMS_FONT_SELECTION_ENABLE(false),
	DMS_PIXEL_STATUS_ENABLE(true),
	DMS_BRIGHTNESS_ENABLE(true),
	DMS_MANUFACTURER_ENABLE(true),
	DMS_RESET_ENABLE(false),
	EMAIL_SMTP_HOST(String.class),
	EMAIL_SENDER_SERVER(String.class),
	EMAIL_SENDER_CLIENT(String.class),
	EMAIL_RECIPIENT_AWS(String.class),
	EMAIL_RECIPIENT_BUGS(String.class),
	HELP_TROUBLE_TICKET_ENABLE(false),
	HELP_TROUBLE_TICKET_URL(String.class),
	INCIDENT_RING_1_MILES(0, 0, 50),
	INCIDENT_RING_2_MILES(0, 0, 50),
	INCIDENT_RING_3_MILES(0, 0, 50),
	INCIDENT_RING_4_MILES(0, 0, 50),
	KML_CREATE_FILE(false),
	KML_FILENAME(String.class),
	MAP_UTM_ZONE(String.class),
	MAP_NORTHERN_HEMISPHERE(true),
	METER_GREEN_SECS(1.3f, 0.1f, 10f),
	METER_YELLOW_SECS(0.7f, 0.1f, 10f),
	METER_MIN_RED_SECS(0.1f, 0.1f, 10f),
	METER_MAX_RED_SECS(13f, 5f, 30f),
	TEMP_FAHRENHEIT_ENABLE(true),
	TESLA_HOST(String.class),
	TRAVEL_TIME_MIN_MPH(15, 1, 50),
	TRAVEL_TIME_MAX_LEGS(8, 1, 20),
	TRAVEL_TIME_MAX_MILES(16, 1, 30),
	UPTIME_LOG_ENABLE(false),
	UPTIME_LOG_FILENAME(String.class);

	/** System attribute class */
	protected final Class atype;

	/** Default value */
	protected final Object def_value;

	/** Minimum value for number attributes */
	protected final Number min_value;

	/** Maximum value for number attributes */
	protected final Number max_value;

	/** Create a String attribute with the given default value */
	private SystemAttrEnum(String d) {
		this(String.class, d, null, null);
	}

	/** Create a Boolean attribute with the given default value */
	private SystemAttrEnum(boolean d) {
		this(Boolean.class, d, null, null);
	}

	/** Create an Integer attribute with default, min and max values */
	private SystemAttrEnum(int d, int mn, int mx) {
		this(Integer.class, d, mn, mx);
	}

	/** Create an Integer attribute with default and min values */
	private SystemAttrEnum(int d, int mn) {
		this(Integer.class, d, mn, null);
	}

	/** Create a Float attribute with default, min and max values */
	private SystemAttrEnum(float d, float mn, float mx) {
		this(Float.class, d, mn, mx);
	}

	/** Create a system attribute with a null default value */
	private SystemAttrEnum(Class c) {
		this(c, null, null, null);
	}

	/** Create a system attribute */
	private SystemAttrEnum(Class c, Object d, Number mn, Number mx) {
		atype = c;
		def_value = d;
		min_value = mn;
		max_value = mx;
		assert isValidBoolean() || isValidFloat() ||
		       isValidInteger() || isValidString();
	}

	/** Test if the attribute is a valid boolean */
	private boolean isValidBoolean() {
		return (atype == Boolean.class) &&
		       (def_value instanceof Boolean) &&
		       min_value == null && max_value == null;
	}

	/** Test if the attribute is a valid float */
	private boolean isValidFloat() {
		return (atype == Float.class) &&
		       (def_value instanceof Float) &&
		       (min_value == null || min_value instanceof Float) &&
		       (max_value == null || max_value instanceof Float);
	}

	/** Test if the attribute is a valid integer */
	private boolean isValidInteger() {
		return (atype == Integer.class) &&
		       (def_value instanceof Integer) &&
		       (min_value == null || min_value instanceof Integer) &&
		       (max_value == null || max_value instanceof Integer);
	}

	/** Test if the attribute is a valid string */
	private boolean isValidString() {
		return (atype == String.class) &&
		       (def_value == null || def_value instanceof String) &&
		       min_value == null && max_value == null;
	}
}
