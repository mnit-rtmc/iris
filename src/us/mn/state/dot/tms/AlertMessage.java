/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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
 * Alert Configuration Message.
 *
 * Associates an alert configuration with an alert period, sign group and quick
 * message to control which signs are eligible for inclusion in an alert and
 * which message template to use.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public interface AlertMessage extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "alert_message";

	/** Get the alert configuration */
	AlertConfig getAlertConfig();

	/** Set the alert period (ordinal of AlertPeriod enum) */
	void setAlertPeriod(int ap);

	/** Get the alert period (ordinal of AlertPeriod enum) */
	int getAlertPeriod();

	/** Set the sign group */
	void setSignGroup(SignGroup sg);

	/** Get the sign group */
	SignGroup getSignGroup();

	/** Set the quick message template */
	void setQuickMessage(QuickMessage qm);

	/** Get the quick message template */
	QuickMessage getQuickMessage();
}
