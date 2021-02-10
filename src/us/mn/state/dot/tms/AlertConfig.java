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
 * Alert Configuration object.
 *
 * Associates alert parameters ("event", "response type", "urgency") to sign
 * group/quick message pairs to control which signs are eligible for inclusion
 * in an alert and which message template to use.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public interface AlertConfig extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "alert_config";

	/** Set the alert event code */
	void setEvent(String ev);

	/** Set the alert event code */
	String getEvent();

	/** Set the response type (ordinal of CapResponseType enum) */
	void setResponseType(int rt);

	/** Get the response type (ordinal of CapResponseType enum) */
	int getResponseType();

	/** Set the urgency (ordinal of CapUrgency enum) */
	void setUrgency(int urg);

	/** Get the urgency (ordinal of CapUrgency enum) */
	int getUrgency();

	/** Set the sign group */
	void setSignGroup(SignGroup sg);

	/** Get the sign group */
	SignGroup getSignGroup();

	/** Set the quick message (template) */
	void setQuickMessage(QuickMessage qm);

	/** Set the quick message (template) */
	QuickMessage getQuickMessage();

	/** Set the number of hours to display a pre-alert message before the
	 *  alert becomes active. */
	void setPreAlertHours(int hours);

	/** Get the number of hours to display a pre-alert message before the
	 *  alert becomes active. */
	int getPreAlertHours();

	/** Set the number of hours to display a post-alert message after an
	 *  alert expires or is cleared. */
	void setPostAlertHours(int hours);

	/** Get the number of hours to display a post-alert message after an
	 *  alert expires or is cleared. */
	int getPostAlertHours();

	/** Enable/disable auto deploy */
	void setAutoDeploy(boolean ad);

	/** Get auto deploy enabled state */
	boolean getAutoDeploy();
}
