/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  SRF Consulting Group
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
 * This table contains an entry for each RWIS-enabled sign 
 * and provides current RWIS status information for each
 * of those signs.
 *
 * @author John L. Stanley - SRF Consulting
 */
public interface RwisSign extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "rwis_sign";

	/** Get comma separated list of RWIS Conditions (null if none) */
	String getRwisConditions();

	/** Set comma separated list of RWIS Conditions (empty string if none) */
	void setRwisConditionsNotify(String rc);

	/** Get Message Pattern Name (null if none) */
	String getMsgPattern();

	/** Set Message Pattern Name (null if none) */
	void setMsgPatternNotify(String mp);
}
