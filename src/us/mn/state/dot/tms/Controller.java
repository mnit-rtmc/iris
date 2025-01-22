/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
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
 * Controller for field devices, etc.
 *
 * @author Douglas Lau
 */
public interface Controller extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "controller";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** All I/O pins */
	int ALL_PINS = 150;

	/** Set the communication link */
	void setCommLink(CommLink l);

	/** Get the communication link */
	CommLink getCommLink();

	/** Set the drop address */
	void setDrop(short d);

	/** Get the drop address */
	short getDrop();

	/** Set the cabinet style */
	void setCabinetStyle(CabinetStyle s);

	/** Get the cabinet style */
	CabinetStyle getCabinetStyle();

	/** Get the location */
	GeoLoc getGeoLoc();

	/** Set the condition */
	void setCondition(int c);

	/** Get the condition */
	int getCondition();

	/** Set the access password */
	void setPassword(String pwd);

	/** Get the access password */
	String getPassword();

	/** Set notes (including hashtags) */
	void setNotes(String n);

	/** Get setup data read from the controller as JSON */
	String getSetup();

	/** Get the current status as JSON */
	String getStatus();

	/** Status JSON attributes */

	/** Fault conditions.
	 *
	 * Semicolon-delimited list of fault conditions:
	 * `other`, `prom`, `program_processor`, `ram`, `display`, `gps` */
	String FAULTS = "faults";

	/** Status message */
	String MSG = "msg";

	/** Get the controller fail time, or null if communication is not
	 * failed.  This time is in milliseconds since the epoch. */
	Long getFailTime();

	/** Get controller location (from GeoLoc) */
	String getLocation();

	/** Get the timeout error count */
	int getTimeoutErr();

	/** Get the checksum error count */
	int getChecksumErr();

	/** Get the parsing error count */
	int getParsingErr();

	/** Get the controller error count */
	int getControllerErr();

	/** Get the successful operation count */
	int getSuccessOps();

	/** Get the failed operation count */
	int getFailedOps();

	/** Request a device operation (send settings, etc.) */
	void setDeviceRequest(int r);
}
