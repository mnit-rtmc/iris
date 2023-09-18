/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
 * Copyright (C) 2009-2015  AHMCT, University of California
 * Copyright (C) 2012-2021  Iteris Inc.
 * Copyright (C) 2015-2020  SRF Consulting Group
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

import java.util.HashMap;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This enum defines all system attributes.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author Travis Swanston
 * @author John L. Stanley
 */
public enum SystemAttrEnum {
	ACTION_PLAN_ALERT_LIST(""),
	ACTION_PLAN_EVENT_PURGE_DAYS(90, 0),
	ALARM_EVENT_PURGE_DAYS(0, 0),
	ALERT_CLEAR_SECS(300, 0, 3600),
	ALERT_SIGN_THRESH_AUTO_METERS(1000, 0),
	ALERT_SIGN_THRESH_OPT_METERS(4000, 0),
	BEACON_EVENT_PURGE_DAYS(0, 0),
	CAMERA_AUTOPLAY(true, Change.RESTART_CLIENT),
	CAMERA_BLANK_URL(String.class),
	CAMERA_CONSTRUCTION_URL(String.class),
	CAMERA_IMAGE_BASE_URL(String.class),
	CAMERA_KBD_PANASONIC_ENABLE(false),
	CAMERA_NUM_BLANK(999, 0, 9999),
	CAMERA_OUT_OF_SERVICE_URL(String.class),
	CAMERA_SEQUENCE_DWELL_SEC(5, 1, 300),
	CAMERA_PRESET_STORE_ENABLE(false, Change.RESTART_CLIENT),
	CAMERA_PTZ_BLIND(true),
	CAMERA_STREAM_CONTROLS_ENABLE(false, Change.RESTART_CLIENT),
	CAMERA_SWITCH_EVENT_PURGE_DAYS(30, 0),
	CAMERA_VIDEO_EVENT_PURGE_DAYS(14, 0),
	CAMERA_WIPER_PRECIP_MM_HR(8, 1, 100),
	CLEARGUIDE_KEY(""),
	CAP_ALERT_PURGE_DAYS(7, 1),
	CAP_XML_SAVE_ENABLE(true),
	CLIENT_EVENT_PURGE_DAYS(0, 0),
	CLIENT_UNITS_SI(true),
	COMM_EVENT_ENABLE(true),
	COMM_EVENT_PURGE_DAYS(14, 0),
	DATABASE_VERSION(String.class),
	DETECTOR_AUTO_FAIL_ENABLE(true),
	DETECTOR_EVENT_PURGE_DAYS(90, 0),
	DETECTOR_OCC_SPIKE_SECS(60, 0, 500),
	DMS_COMM_LOSS_ENABLE(true),
	DMS_GPS_JITTER_M(100, 0, 2000),
	DMS_LAMP_TEST_TIMEOUT_SECS(30, 5, 90),
	DMS_PAGE_ON_MAX_SECS(10.0f, 0f, 100f, Change.RESTART_CLIENT),
	DMS_PAGE_ON_MIN_SECS(0.5f, 0f, 100f, Change.RESTART_CLIENT),
	DMS_PIXEL_OFF_LIMIT(2, 1),
	DMS_PIXEL_ON_LIMIT(1, 1),
	DMS_PIXEL_MAINT_THRESHOLD(35, 1),
	DMS_PIXEL_TEST_TIMEOUT_SECS(30, 5, 90),
	DMS_SEND_CONFIRMATION_ENABLE(false, Change.RESTART_CLIENT),
	DMS_UPDATE_FONT_TABLE(true),
	DMSXML_REINIT_DETECT(false),
	EMAIL_RATE_LIMIT_HOURS(0, 0),
	EMAIL_RECIPIENT_ACTION_PLAN(String.class),
	EMAIL_RECIPIENT_AWS(String.class),
	EMAIL_RECIPIENT_DMSXML_REINIT(String.class),
	EMAIL_RECIPIENT_GATE_ARM(String.class),
	EMAIL_SENDER_SERVER(String.class),
	EMAIL_SMTP_HOST(String.class),
	GATE_ARM_ALERT_TIMEOUT_SECS(90, 10),
	GATE_ARM_EVENT_PURGE_DAYS(0, 0),
	HELP_TROUBLE_TICKET_ENABLE(false),
	HELP_TROUBLE_TICKET_URL(String.class),
	INCIDENT_CLEAR_ADVICE_MULTI(String.class),
	INCIDENT_CLEAR_SECS(300, 0, 3600),
	MAP_EXTENT_NAME_INITIAL("Home"),
	MAP_ICON_SIZE_SCALE_MAX(30f, 0f, 9000f),
	MAP_SEGMENT_MAX_METERS(2000, 100, Change.RESTART_CLIENT),
	METER_EVENT_ENABLE(true),
	METER_EVENT_PURGE_DAYS(14, 0),
	METER_GREEN_SECS(1.3f, 0.1f, 10f),
	METER_MAX_RED_SECS(13f, 5f, 30f),
	METER_MIN_RED_SECS(0.1f, 0.1f, 10f),
	METER_YELLOW_SECS(0.7f, 0.1f, 10f),
	MSG_FEED_VERIFY(true),
	OPERATION_RETRY_THRESHOLD(3, 1, 20),
	PRICE_MESSAGE_EVENT_PURGE_DAYS(0, 0),
	ROUTE_MAX_LEGS(8, 1, 20),
	ROUTE_MAX_MILES(16, 1, 30),
	RWIS_HIGH_WIND_SPEED_KPH(40, 0),
	RWIS_LOW_VISIBILITY_DISTANCE_M(152, 0),
	RWIS_OBS_AGE_LIMIT_SECS(240, 0),
	RWIS_MAX_VALID_WIND_SPEED_KPH(282, 0),
	SAMPLE_ARCHIVE_ENABLE(true),
	SIGN_EVENT_PURGE_DAYS(0, 0),
	SPEED_LIMIT_MIN_MPH(45, 0, 100),
	SPEED_LIMIT_DEFAULT_MPH(55, 0, 100),
	SPEED_LIMIT_MAX_MPH(75, 0, 100),
	TAG_READ_EVENT_PURGE_DAYS(0, 0),
	TOLL_DENSITY_ALPHA(0.045f, 0.01f, 4.0f),
	TOLL_DENSITY_BETA(1.10f, 0.01f, 4.0f),
	TOLL_MIN_PRICE(0.25f, 0f, 100f),
	TOLL_MAX_PRICE(8f, 1f, 100f),
	TRAVEL_TIME_MIN_MPH(15, 1, 50),
	UPTIME_LOG_ENABLE(false),
	VID_CONNECT_AUTOSTART(true),
	VID_CONNECT_FAIL_NEXT_SOURCE(true),
	VID_CONNECT_FAIL_SEC(20, 0, 32000),
	VID_LOST_TIMEOUT_SEC(10, 0, 32000),
	VID_RECONNECT_AUTO(true),
	VID_RECONNECT_TIMEOUT_SEC(10, 0, 32000),
	VSA_BOTTLENECK_ID_MPH(55, 10, 65),
	VSA_CONTROL_THRESHOLD(-1000, -5000, -200),
	VSA_DOWNSTREAM_MILES(0.2f, 0f, 2.0f),
	VSA_MAX_DISPLAY_MPH(60, 10, 60),
	VSA_MIN_DISPLAY_MPH(30, 10, 55),
	VSA_MIN_STATION_MILES(0.1f, 0.01f, 1.0f),
	VSA_START_INTERVALS(3, 0, 10),
	VSA_START_THRESHOLD(-1500, -5000, -200),
	VSA_STOP_THRESHOLD(-750, -5000, -200),
	WEATHER_SENSOR_EVENT_PURGE_DAYS(90, 0),
	WINDOW_TITLE("IRIS: ", Change.RESTART_CLIENT),
	WORK_REQUEST_URL(String.class);

	/** Change action, which indicates what action the admin must
	 *  take after changing a system attribute. */
	enum Change {
		RESTART_CLIENT("Restart the client after changing."),
		NONE("A change takes effect immediately.");

		/** Change message for user. */
		private final String m_msg;

		/** Constructor */
		private Change(String msg) {
			m_msg = msg;
		}

		/** Get the restart message. */
		public String getMessage() {
			return m_msg;
		}
	}

	/** System attribute class */
	private final Class atype;

	/** Default value */
	private final Object def_value;

	/** Change action */
	private final Change change_action;

	/** Minimum value for number attributes */
	private final Number min_value;

	/** Maximum value for number attributes */
	private final Number max_value;

	/** Create a String attribute with the given default value */
	private SystemAttrEnum(String d) {
		this(String.class, d, null, null, Change.NONE);
	}

	/** Create a String attribute with the given default value */
	private SystemAttrEnum(String d, Change ca) {
		this(String.class, d, null, null, ca);
	}

	/** Create a Boolean attribute with the given default value */
	private SystemAttrEnum(boolean d) {
		this(Boolean.class, d, null, null, Change.NONE);
	}

	/** Create a Boolean attribute with the given default value */
	private SystemAttrEnum(boolean d, Change ca) {
		this(Boolean.class, d, null, null, ca);
	}

	/** Create an Integer attribute with default, min and max values */
	private SystemAttrEnum(int d, int mn, int mx) {
		this(Integer.class, d, mn, mx, Change.NONE);
	}

	/** Create an Integer attribute with default, min and max values */
	private SystemAttrEnum(int d, int mn, int mx, Change ca) {
		this(Integer.class, d, mn, mx, ca);
	}

	/** Create an Integer attribute with default and min values */
	private SystemAttrEnum(int d, int mn) {
		this(Integer.class, d, mn, null, Change.NONE);
	}

	/** Create an Integer attribute with default and min values */
	private SystemAttrEnum(int d, int mn, Change ca) {
		this(Integer.class, d, mn, null, ca);
	}
	
	/** Create a Float attribute with a default value */
	private SystemAttrEnum(float d) {
		this(Float.class, d, null, null, Change.NONE);
	}

	/** Create a Float attribute with default, min and max values */
	private SystemAttrEnum(float d, float mn, float mx) {
		this(Float.class, d, mn, mx, Change.NONE);
	}

	/** Create a Float attribute with default, min and max values */
	private SystemAttrEnum(float d, float mn, float mx, Change ca) {
		this(Float.class, d, mn, mx, ca);
	}

	/** Create a system attribute with a null default value */
	private SystemAttrEnum(Class c) {
		this(c, null, null, null, Change.NONE);
	}

	/** Create a system attribute with a null default value */
	private SystemAttrEnum(Class c, Change ca) {
		this(c, null, null, null, ca);
	}

	/** Create a system attribute */
	private SystemAttrEnum(Class c, Object d, Number mn, Number mx,
		Change ca)
	{
		atype = c;
		def_value = d;
		min_value = mn;
		max_value = mx;
		change_action = ca;
		assert isValidBoolean() || isValidFloat() ||
		       isValidInteger() || isValidString();
	}

	/** Get a description of the system attribute enum. */
	public static String getDesc(String aname) {
		String ret = I18N.get(aname);
		SystemAttrEnum sae = lookup(aname);
		if (sae != null)
			ret += " " + sae.change_action.getMessage();
		return ret;
	}

	/** Return true if the value is the default value. */
	public boolean equalsDefault() {
		return get().toString().equals(getDefault());
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

	/** Get the attribute name */
	public String aname() {
		return toString().toLowerCase();
	}

	/** Set of all system attributes */
	static private final HashMap<String, SystemAttrEnum> ALL_ATTRIBUTES =
		new HashMap<String, SystemAttrEnum>();
	static {
		for (SystemAttrEnum sa: SystemAttrEnum.values())
			ALL_ATTRIBUTES.put(sa.aname(), sa);
	}

	/** Lookup an attribute by name */
	static public SystemAttrEnum lookup(String aname) {
		return ALL_ATTRIBUTES.get(aname);
	}

	/**
	 * Get the value of the attribute as a string.
	 * @return The value of the attribute as a string, never null.
	 */
	public String getString() {
		assert atype == String.class;
		return (String) get();
	}

	/** Get the default value as a String. */
	public String getDefault() {
		if (def_value != null)
			return def_value.toString();
		else
			return "";
	}

	/** Get the value of the attribute as a boolean */
	public boolean getBoolean() {
		assert atype == Boolean.class;
		return (Boolean) get();
	}

	/** Get the value of the attribute as an int */
	public int getInt() {
		assert atype == Integer.class;
		return (Integer) get();
	}

	/** Get the value of the attribute as a float */
	public float getFloat() {
		assert atype == Float.class;
		return (Float) get();
	}

	/**
	 * Get the value of the attribute.
	 * @return The value of the attribute, never null.
	 */
	private Object get() {
		return getValue(SystemAttributeHelper.get(aname()));
	}

	/**
	 * Get the value of a system attribute.
	 * @param attr System attribute or null.
	 * @return The attribute value or the default value on error.
	 *         Null is never returned.
	 */
	private Object getValue(SystemAttribute attr) {
		if (attr == null) {
			System.err.println(warningDefault());
			return def_value;
		}
		return parseValue(attr.getValue());
	}

	/**
	 * Get the value of a system attribute.
	 * @return The parsed value or the default value on error.
	 *         Null is never returned.
	 */
	public Object parseValue(String v) {
		Object value = parse(v);
		if (value == null) {
			System.err.println(warningParse());
			return def_value;
		}
		return value;
	}

	/**
	 * Parse an attribute value.
	 * @param v Attribute value, may be null.
	 * @return The parsed value or null on error.
	 */
	private Object parse(String v) {
		if (atype == String.class)
			return v;
		if (atype == Boolean.class)
			return parseBoolean(v);
		if (atype == Integer.class)
			return parseInteger(v);
		if (atype == Float.class)
			return parseFloat(v);
		assert false;
		return null;
	}

	/** Parse a boolean attribute value */
	private Boolean parseBoolean(String v) {
		try {
			return Boolean.parseBoolean(v);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Parse an integer attribute value */
	private Integer parseInteger(String v) {
		int i;
		try {
			i = Integer.parseInt(v);
		}
		catch (NumberFormatException e) {
			return null;
		}
		if (min_value != null) {
			int m = min_value.intValue();
			if (i < m) {
				System.err.println(warningMinimum());
				return m;
			}
		}
		if (max_value != null) {
			int m = max_value.intValue();
			if (i > m) {
				System.err.println(warningMaximum());
				return m;
			}
		}
		return i;
	}

	/** Parse a float attribute value */
	private Float parseFloat(String v) {
		float f;
		try {
			f = Float.parseFloat(v);
		}
		catch (NumberFormatException e) {
			return null;
		}
		if (min_value != null) {
			float m = min_value.floatValue();
			if (f < m) {
				System.err.println(warningMinimum());
				return m;
			}
		}
		if (max_value != null) {
			float m = max_value.floatValue();
			if (f > m) {
				System.err.println(warningMaximum());
				return m;
			}
		}
		return f;
	}

	/** Create a 'missing system attribute' warning message */
	private String warningDefault() {
		return "Warning: " + toString() + " system attribute was not " +
		       "found; using a default value (" + def_value + ").";
	}

	/** Create a parsing warning message */
	private String warningParse() {
		return "Warning: " + toString() + " system attribute could " +
		       "not be parsed; using a default value (" +
			def_value + ").";
	}

	/** Create a minimum value warning message */
	private String warningMinimum() {
		return "Warning: " + toString() + " system attribute was too " +
		       "low; using a minimum value (" + min_value + ").";
	}

	/** Create a maximum value warning message */
	private String warningMaximum() {
		return "Warning: " + toString() + " system attribute was too " +
		       "high; using a maximum value (" + max_value + ").";
	}
}
