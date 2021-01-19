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

import java.util.Date;
import java.util.List;
import org.postgis.MultiPolygon;
import us.mn.state.dot.sonar.SonarObject;

/**
 * Integrated Public Alert and Warning System (IPAWS) Alert interface.
 *
 * @author Michael Janson
 * @author Gordon Parikh
 */
public interface IpawsAlert extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "ipaws_alert";

	/** Get the identifier */
	String getIdentifier();

	/** Get the sender */
	String getSender();

	/** Get the sent date */
	Date getSentDate();

	/** Get the status */
	String getStatus();

	/** Get the message type */
	String getMsgType();

	/** Get the scope */
	String getScope();

	/** Get the codes */
	List<String> getCodes();

	/** Get the note */
	String getNote();

	/** Get the alert references */
	List<String> getAlertReferences();

	/** Get the incidents */
	List<String> getIncidents();

	/** Get the categories */
	List<String> getCategories();

	/** Get the event */
	String getEvent();

	/** Get the response types */
	List<String> getResponseTypes();

	/** Get the urgency */
	String getUrgency();

	/** Get the severity */
	String getSeverity();

	/** Get the certainty */
	String getCertainty();

	/** Get the audience */
	String getAudience();

	/** Get the effective date */
	Date getEffectiveDate();

	/** Get the onset date */
	Date getOnsetDate();

	/** Get the expiration date */
	Date getExpirationDate();

	/** Get the sender name */
	String getSenderName();

	/** Get the headline */
	String getHeadline();

	/** Get the alert description */
	String getAlertDescription();

	/** Get the alert instruction */
	String getInstruction();

	/** Get the parameters */
	String getParameters();

	/** Get the areas */
	String getArea();

	/** Get the geographic polygon of the area */
	MultiPolygon getGeoPoly();

	/** Get the latitude of the alert area's centroid */
	Double getLat();

	/** Get the longitude of the alert area's centroid */
	Double getLon();

	/** Flag indicating if this alert is purgeable (irrelevant to us) */
	Boolean getPurgeable();

	/** Get the last processing time of the alert */
	Date getLastProcessed();
}
