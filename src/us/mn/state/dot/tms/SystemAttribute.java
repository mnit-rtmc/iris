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
 * A system attribute is a name mapped to a string value.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public interface SystemAttribute extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "system_attribute";

	/** attribute names common to all agencies */
	// note: please append units to names!
	final String DATABASE_VERSION = "database_version";
	final String DMS_POLL_FREQ_SECS = "dms_poll_freq_secs";
	final String AGENCY_ID = "agency_id";

	/** DMSDispatcher */
	final String DMSDISPATCHER_GETSTATUS_BTN = 
		"dmsdispatcher_getstatus_btn";

	/** CameraViewer */
	final String CAMERAVIEWER_ONSCRN_PTZCTRLS = 
		"cameraviewer_onscrn_ptzctrls";
	final String CAMERAVIEWER_NUM_PRESET_BTNS = 
		"cameraviewer_num_preset_btns";

	/** possible values for the AGENCY_ID attribute */
	final String AGENCY_ID_MNDOT = "mndot";
	final String AGENCY_ID_CALTRANS_D10 = "caltrans_d10";

	/** Caltrans D10 specific values */
	final String CALTRANS_D10_CAWS_ACTIVE = 
		"caltrans_d10_caws_active";
	final String CALTRANS_D10_DMSLITE_OP_TIMEOUT_SECS = 
		"caltrans_d10_op_timeout_secs";
	final String CALTRANS_D10_DMSLITE_MODEM_OP_TIMEOUT_SECS = 
		"caltrans_d10_modem_op_timeout_secs";

	/** Set the attribute value */
	void setValue(String arg_value);

	/** Get the attribute value */
	String getValue();
}
