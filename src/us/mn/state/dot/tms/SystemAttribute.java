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

import us.mn.state.dot.sonar.SonarObject;

/**
 * A system attribute is a name mapped to a string value. This interface
 * contains predefined names of attributes common to all agencies. 
 * Attributes specific to an agency should be placed in an extended
 * helper class.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public interface SystemAttribute extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "system_attribute";

	/** maximum length of an attribute name */
	int MAXLEN_ANAME = 32;

	/** attribute names common to all agencies */
	String DATABASE_VERSION = "database_version";

	/** DMS page on time */
	String DMS_PAGE_ON_SECS = "dms_page_on_secs";

	/** DMS page off time */
	String DMS_PAGE_OFF_SECS = "dms_page_off_secs";

	/** DMS status polling frequency */
	String DMS_POLL_FREQ_SECS = "dms_poll_freq_secs";

	/** DMS maximum number of lines */
	String DMS_MAX_LINES = "dms_max_lines";

	/** DMS pixel off limit (in a message) */
	String DMS_PIXEL_OFF_LIMIT = "dms_pixel_off_limit";

	/** DMS pixel on limit (near a message) */
	String DMS_PIXEL_ON_LIMIT = "dms_pixel_on_limit";

	/** DMS high temp cutoff (degrees Celsius) */
	String DMS_HIGH_TEMP_CUTOFF = "dms_high_temp_cutoff";

	/** DMS default line justification */
	String DMS_DEFAULT_JUSTIFICATION_LINE ="dms_default_justification_line";

	/** DMS default page justification */
	String DMS_DEFAULT_JUSTIFICATION_PAGE ="dms_default_justification_page";

	/** DMS message minimum available pages */
	String DMS_MESSAGE_MIN_PAGES = "dms_message_min_pages";

	/** DMS enable blank lines in messages (boolean) */
	String DMS_MESSAGE_BLANK_LINE_ENABLE = "dms_message_blank_line_enable";

	/** DMS travel time duration (minutes) */
	String DMS_TRAVEL_DURATION_MINS = "dms_travel_duration_mins";

	/** DMS enable extra status (boolean) */
	String DMS_STATUS_ENABLE = "dms_status_enable";

	/** DMS enable automated warning system (boolean) */
	String DMS_AWS_ENABLE = "dms_aws_enable";

	/** DMS enable duration selection (boolean) */
	String DMS_DURATION_ENABLE = "dms_duration_enable";

	/** DMS enable font selection (boolean) */
	String DMS_FONT_SELECTION_ENABLE = "dms_font_selection_enable";

	/** DMS enable pixel status (boolean) */
	String DMS_PIXEL_STATUS_ENABLE = "dms_pixel_status_enable";

	/** DMS enable brightness tab (boolean) */
	String DMS_BRIGHTNESS_ENABLE = "dms_brightness_enable";

	/** DNS enable manufacturer-specific tab (boolean) */
	String DMS_MANUFACTURER_ENABLE = "dms_manufacturer_enable";

	/** DMS enable reset (boolean) */
	String DMS_RESET_ENABLE = "dms_reset_enable";

	/** Temperature fahrenheit enable (boolean) */
	String TEMP_FAHRENHEIT_ENABLE = "temp_fahrenheit_enable";

	/** Meter green time */
	String METER_GREEN_SECS = "meter_green_secs";

	/** Meter yellow time */
	String METER_YELLOW_SECS = "meter_yellow_secs";

	/** Meter mimimum red time */
	String METER_MIN_RED_SECS = "meter_min_red_secs";

	/** Meter maximum red time */
	String METER_MAX_RED_SECS = "meter_max_red_secs";

	/** Incident ring 1 radius */
	String INCIDENT_RING_1_MILES = "incident_ring_1_miles";

	/** Incident ring 2 radius */
	String INCIDENT_RING_2_MILES = "incident_ring_2_miles";

	/** Incident ring 3 radius */
	String INCIDENT_RING_3_MILES = "incident_ring_3_miles";

	/** Incident ring 4 radius */
	String INCIDENT_RING_4_MILES = "incident_ring_4_miles";

	/** Minimum overall trip speed for a travel time estimate (mph) */
	String TRAVEL_TIME_MIN_MPH = "travel_time_min_mph";

	/** Maximum number of legs in a travel time route */
	String TRAVEL_TIME_MAX_LEGS = "travel_time_max_legs";

	/** Maximum route distance in a travel time route (miles) */
	String TRAVEL_TIME_MAX_MILES = "travel_time_max_miles";

	/** TESLA host name (and TCP port) */
	String TESLA_HOST = "tesla_host";

	/** Camera enable PTZ panel (boolean) */
	String CAMERA_PTZ_PANEL_ENABLE = "camera_ptz_panel_enable";

	/** CameraViewer */
	String CAMERAVIEWER_NUM_PRESET_BTNS = "cameraviewer_num_preset_btns";
	String CAMERAVIEWER_NUM_VIDEO_FRAMES = "cameraviewer_num_video_frames";

	String AGENCY_ID = "agency_id";

	/** possible values for the AGENCY_ID attribute */
	String AGENCY_ID_MNDOT = "mndot";
	String AGENCY_ID_CALTRANS_D10 = "caltrans_d10";

	/** Set the attribute value */
	void setValue(String arg_value);

	/** Get the attribute value */
	String getValue();
}
