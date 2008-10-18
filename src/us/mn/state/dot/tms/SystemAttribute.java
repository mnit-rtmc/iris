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
	final int MAXLEN_ANAME = 32;

	/** attribute names common to all agencies */
	// note: please append units to names!
	final String DATABASE_VERSION = "database_version";
	final String DMS_POLL_FREQ_SECS = "dms_poll_freq_secs";
	final String AGENCY_ID = "agency_id";

	/** DMSDispatcher */
	final String DMSDISPATCHER_GETSTATUS_BTN = 
		"dmsdispatcher_getstatus_btn";

	/** DMS */
	final String DMS_PREFERRED_FONT = 
		"dms_preferred_font";

	/** CameraViewer */
	final String CAMERAVIEWER_ONSCRN_PTZCTRLS = 
		"cameraviewer_onscrn_ptzctrls";
	final String CAMERAVIEWER_NUM_PRESET_BTNS = 
		"cameraviewer_num_preset_btns";
	final String CAMERAVIEWER_NUM_VIDEO_FRAMES = 
		"cameraviewer_num_video_frames";

	/** possible values for the AGENCY_ID attribute */
	final String AGENCY_ID_MNDOT = "mndot";
	final String AGENCY_ID_CALTRANS_D10 = "caltrans_d10";

	/** Set the attribute value */
	void setValue(String arg_value);

	/** Get the attribute value */
	String getValue();
}
