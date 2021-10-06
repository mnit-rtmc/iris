/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2021  Minnesota Department of Transportation
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
 * Cabinet styles
 *
 * @author Douglas Lau
 */
public interface CabinetStyle extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "cabinet_style";

	/** Set the police panel input pin for meter 1 */
	void setPolicePanelPin1(Integer p);

	/** Get the police panel input pin for meter 1 */
	Integer getPolicePanelPin1();

	/** Set the police panel input pin for meter 2 */
	void setPolicePanelPin2(Integer p);

	/** Get the police panel input pin for meter 2 */
	Integer getPolicePanelPin2();

	/** Set the watchdog reset pin for meter 1 */
	void setWatchdogResetPin1(Integer p);

	/** Get the watchdog reset pin for meter 1 */
	Integer getWatchdogResetPin1();

	/** Set the watchdog reset pin for meter 2 */
	void setWatchdogResetPin2(Integer p);

	/** Get the watchdog reset pin for meter 2 */
	Integer getWatchdogResetPin2();

	/** Set the DIP switch value */
	void setDip(Integer d);

	/** Get the DIP switch value */
	Integer getDip();
}
