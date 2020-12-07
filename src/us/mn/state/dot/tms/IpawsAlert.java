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
	String SONAR_TYPE = "ipaws";
	
	/** Set the identifier */
	void setIdentifier(String i);

	/** Get the identifier */
	String getIdentifier();
	
	/** Set the sender */
	void setSender(String se);
	
	/** Get the sender */
	String getSender();
	
	/** Set the sent date */
	void setSentDate(Date sd);
	
	/** Get the sent date */
	Date getSentDate();

	/** Set the status */
	void setStatus(String sta);

	/** Get the status */
	String getStatus();
	
	/** Set the message type */
	void setMsgType(String mt);
	
	/** Get the message type */
	String getMsgType();
	
	/** Set the scope */
	void setScope(String sc);
	
	/** Get the scope */
	String getScope();	

	/** Set the codes */
	void setCodes(List<String> cd);
	
	/** Get the codes */
	List<String> getCodes();
	
	/** Set the note */
	void setNote(String nt);
	
	/** Get the note */
	String getNote();
	
	/** Set the alert references */
	void setAlertReferences(List<String> ref);
	
	/** Get the alert references */
	List<String> getAlertReferences();
	
	/** Set the incidents */
	void setIncidents(List<String> inc);
	
	/** Get the incidents */
	List<String> getIncidents();
	
	/** Set the categories */
	void setCategories(List<String> ct);
	
	/** Get the categories */
	List<String> getCategories();

	/** Set the event */
	void setEvent(String ev);
	
	/** Get the event */
	String getEvent();
	
	/** Set the response types */
	void setResponseTypes(List<String> rt);
	
	/** Get the response types */
	List<String> getResponseTypes();
	
	/** Set the urgency */
	void setUrgency(String u);
	
	/** Get the urgency */
	String getUrgency();
	
	/** Set the severity */
	void setSeverity(String sv);
	
	/** Get the severity */
	String getSeverity();
	
	/** Set the certainty */
	void setCertainty(String cy);
	
	/** Get the certainty */
	String getCertainty();
	
	/** Set the audience */
	void setAudience(String au);
	
	/** Get the audience */
	String getAudience();

	/** Set the effective date */
	void setEffectiveDate(Date efd);
	
	/** Get the effective date */
	Date getEffectiveDate();

	/** Set the onset date */
	void setOnsetDate(Date od);
	
	/** Get the onset date */
	Date getOnsetDate();
	
	/** Set the expiration date */
	void setExpirationDate(Date exd);
	
	/** Get the expiration date */
	Date getExpirationDate();
	
	/** Set the sender name */
	void setSenderName(String sn);
	
	/** Get the sender name */
	String getSenderName();
	
	/** Set the headline */
	void setHeadline(String hl);
	
	/** Get the headline */
	String getHeadline();
	
	/** Set the alert description */
	void setAlertDescription(String ad);
	
	/** Get the alert description */
	String getAlertDescription();
	
	/** Set the alert instruction */
	void setInstruction(String in);
	
	/** Get the alert instruction */
	String getInstruction();
	
	/** Set the parameters */
	void setParameters(String par);
	
	/** Get the parameters */
	String getParameters();
	
	/** Set the areas */
	void setArea(String ar);
	
	/** Get the areas */
	String getArea();
	
	/** Set the alert polygon */
	void setGeoPoly(MultiPolygon gp);
	
	/** Set the alert polygon from a string */
	void setGeoPoly(String gpstr);
	
	/** Get the areas */
	MultiPolygon getGeoPoly();

	/** Set the GeoLoc, which is the alert area's centroid */
	void setGeoLoc(GeoLoc gl);
	
	/** Get the GeoLoc, which is the alert area's centroid */
	GeoLoc getGeoLoc();
	
	/** Set if this alert is purgeable (irrelevant to us). Also acts as a flag
	 *  to indicate whether an alert has been processed or not (null if not
	 *  processed yet).
	 */
	void setPurgeable(Boolean p);
	
	/** Return if this alert is purgeable (irrelevant to us) */
	Boolean getPurgeable();
	
	/** Set the last processing time of the alert */
	void setLastProcessed(Date pt);
	
	/** Get the last processing time of the alert */
	Date getLastProcessed();
}
