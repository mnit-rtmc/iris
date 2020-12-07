/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
 * IPAWS Alert Configuration object. Connects a particular alert type ("event"
 * field) to a number of sign group/quick message pairs to control which signs
 * are eligible for inclusion in an alert and which message template to use. 
 *
 * @author Gordon Parikh
 */
public interface IpawsAlertConfig extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "ipaws_alert_config";
	
	/** Set the alert event type */
	void setEvent(String ev);
	
	/** Set the alert event type */
	String getEvent();
	
	/** Set the sign group */
	void setSignGroup(String sg);
	
	/** Get the sign group */
	String getSignGroup();
	
	/** Set the quick message (template) */
	void setQuickMessage(String qm);
	
	/** Set the quick message (template) */
	String getQuickMessage();
	
	/** Set amount of time (in hours) to display a pre-alert message before
	 *  the alert becomes active.
	 */
	void setPreAlertTime(int hours);

	/** Get amount of time (in hours) to display a pre-alert message before
	 *  the alert becomes active.
	 */
	int getPreAlertTime();
	
	/** Set amount of time (in hours) to display a post-alert message after
	 *  an alert expires or an AllClear response type is sent via IPAWS.
	 */
	void setPostAlertTime(int hours);
	
	/** Get amount of time (in hours) to display a post-alert message after
	 *  an alert expires or an AllClear response type is sent via IPAWS.
	 */
	int getPostAlertTime();
}