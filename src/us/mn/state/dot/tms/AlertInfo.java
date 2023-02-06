/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021-2023  Minnesota Department of Transportation
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

import java.util.Date;
import org.postgis.MultiPolygon;
import us.mn.state.dot.sonar.SonarObject;

/**
 * CAP Alert Information is an object which is created when a CAP alert triggers
 * one or more DMS to be deployed.
 *
 * @author Michael Janson
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public interface AlertInfo extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "alert_info";

	/** Get the alert identifier */
	String getAlert();

	/** Get name of alert info this replaces */
	String getReplaces();

	/** Get the start date */
	Date getStartDate();

	/** Get the end date */
	Date getEndDate();

	/** Get the CAP event code (CapEvent name) */
	String getEvent();

	/** Get the CAP response type (CapResponseType ordinal) */
	int getResponseType();

	/** Get the CAP urgency (CapUrgency ordinal) */
	int getUrgency();

	/** Get the CAP severity (CapSeverity ordinal) */
	int getSeverity();

	/** Get the CAP certainty (CapCertainty ordinal) */
	int getCertainty();

	/** Get the headline */
	String getHeadline();

	/** Get the description */
	String getDescription();

	/** Get the alert instruction */
	String getInstruction();

	/** Get the area description */
	String getAreaDesc();

	/** Get the geographic polygon of the area */
	MultiPolygon getGeoPoly();

	/** Get the latitude of the alert area's centroid */
	double getLat();

	/** Get the longitude of the alert area's centroid */
	double getLon();

	/** Get the DMS hashtag for all auto and optional signs */
	String getAllHashtag();

	/** Get the action plan */
	ActionPlan getActionPlan();

	/** Get the alert state (AlertState ordinal) */
	int getAlertState();

	/** Set the alert state (AlertState ordinal) */
	void setAlertStateReq(int st);
}
