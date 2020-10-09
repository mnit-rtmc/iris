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

import us.mn.state.dot.sonar.SonarObject;

/**
 * An IpawsAlertDeployer is an object for notifying clients about new or
 * updated alerts from the Integrated Public Alert and Warning System (IPAWS).
 * 
 * These objects contain a reference to the alert (via the alert_id), the list
 * of DMS that were selected based on the alert area, the MULTI of the message
 * that was generated, and the name of the user that approved the alert (which
 * is null before approval or AUTO when approval mode is disabled).
 *
 * @author Gordon Parikh
 */
public interface IpawsAlertDeployer extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "ipaws_alert_deployer";
	
	/** Set the generation time of this deployer object */
	void setGenTime(Date gt);
	
	/** Get the generation time of this deployer object */
	Date getGenTime();
	
	/** Set the approval time of this deployer object */
	void setApprovedTime(Date at);
	
	/** Get the approval time of this deployer object */
	Date getApprovedTime();
	
	/** Set the Alert ID. */
	void setAlertId(String aid);

	/** Get the Alert ID. */
	String getAlertId();
	
	/** Set the deployer GeoLoc. */
	void setGeoLoc(GeoLoc gl);
	
	/** Get the deployer GeoLoc. */
	GeoLoc getGeoLoc();

	/** Set the alert start time */
	void setAlertStart(Date t);
	
	/** Get the alert start time */
	Date getAlertStart();
	
	/** Set the alert end time */
	void setAlertEnd(Date t);
	
	/** Get the alert end time */
	Date getAlertEnd();
	
	/** Set the config used for this deployment */
	void setConfig(String c);
	
	/** Get the config used for this deployment */
	String getConfig();	
	
	/** Set the sign group used for this deployment */
	void setSignGroup(String sg);
	
	/** Get the sign group used for this deployment */
	String getSignGroup();
	
	/** Set the quick message (template) used for this deployment */
	void setQuickMessage(String qm);
	
	/** Get the quick message (template) used for this deployment */
	String getQuickMessage();

	/** Set amount of time (in hours) to display a pre-alert message before
	 *  the alert becomes active. First set from the config, then can be
	 *  changed for each alert.
	 */
	void setPreAlertTime(int hours);

	/** Get amount of time (in hours) to display a pre-alert message before
	 *  the alert becomes active. First set from the config, then can be
	 *  changed for each alert.
	 */
	int getPreAlertTime();
	
	/** Set amount of time (in hours) to display a post-alert message after
	 *  an alert expires or an AllClear response type is sent via IPAWS. First
	 *  set from the config, then can be changed for each alert.
	 */
	void setPostAlertTime(int hours);
	
	/** Get amount of time (in hours) to display a post-alert message after
	 *  an alert expires or an AllClear response type is sent via IPAWS. First
	 *  set from the config, then can be changed for each alert.
	 */
	int getPostAlertTime();
	
	/** Set the list of DMS (represented as a string array) automatically 
	 *  selected for deploying alert messages.
	 */
	void setAutoDms(String[] dms);
	
	/** Get the list of DMS (represented as a string array) automatically
	 *  selected for deploying alert messages. 
	 */
	String[] getAutoDms();
	
	/** Set the list of DMS suggested automatically by the system as optional 
	 *  DMS that users may want to include for the deployment.
	 */
	void setOptionalDms(String[] dms);
	
	/** Get the list of DMS suggested automatically by the system as optional 
	 *  DMS that users may want to include for the deployment. 
	 */
	String[] getOptionalDms();
	
	/** Set the list of DMS actually used to deploy the message. */
	void setDeployedDms(String[] dms);
	
	/** Get the list of DMS actually used to deploy the message. */
	String[] getDeployedDms();
	
	/** Set area threshold used for including DMS outside the alert area.
	 *  TODO this may become editable per-alert. */
	void setAreaThreshold(Double t);
	
	/** Get area threshold used for including DMS outside the alert area.
	 *  TODO this may become editable per-alert. */
	Double getAreaThreshold();
	
	/** Set the MULTI generated automatically by the system for deploying to
	 *  DMS.
	 */
	void setAutoMulti(String m);
	
	/** Get the MULTI generated automatically by the system for deploying to
	 *  DMS.
	 */
	String getAutoMulti();
	
	/** Set the MULTI actually deployed to DMS. */
	void setDeployedMulti(String m);
	
	/** Get the MULTI actually deployed to DMS. */
	String getDeployedMulti();
	
	/** Set the message priority */
	void setMsgPriority(Integer p);
	
	/** Get the message priority */
	Integer getMsgPriorty();
	
	/** Set the approving user. */
	void setApprovedBy(String u);
	
	/** Get the approving user. */
	String getApprovedBy();
	
	/** Set the deployed state of this alert (whether it is currently
	 *  deployed). This value is null if no action has been taken (i.e.
	 *  approval is required but has not yet been given). Changing this to
	 *  true triggers deployments or updates, and changing it to false
	 *  triggers canceling of an alert deployment.
	 */
	void setDeployed(Boolean d);
	
	/** Get the deployed state of this alert (whether it is currently
	 *  deployed). */
	Boolean getDeployed();

	/** Set whether this alert deployer was ever deployed or not. Note that
	 *  this will be true if an alert message is successfully sent to at least
	 *  one sign. */
	void setWasDeployed(boolean wd);
	
	/** Get whether this alert deployer was ever deployed or not. */
	boolean getWasDeployed();
	
	/** Set whether this alert deployment is currently active (i.e. visible on
	 *  DMS). Alerts can be deployed but not active if they have been approved
	 *  for deployment but the pre-alert time value indicates that they should
	 *  not be deployed yet.
	 */
	void setActive(boolean a);

	/** Get whether this alert deployment is currently active (i.e. visible on
	 *  DMS). Alerts can be deployed but not active if they have been approved
	 *  for deployment but the pre-alert time value indicates that they should
	 *  not be deployed yet.
	 */
	boolean getActive();
	
	/** Set the alert deployer that this replaces (if any). Note that updates
	 *  to alerts trigger creation of a new deployer (not an update).
	 */
	void setReplaces(String r);
	
	/** Get the alert deployer that this replaces (if any). */
	String getReplaces();
}
