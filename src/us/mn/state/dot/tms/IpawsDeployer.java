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
import us.mn.state.dot.sonar.SonarObject;

/**
 * An IpawsDeployer is an object for notifying clients about new or updated
 * alerts from the Integrated Public Alert and Warning System (IPAWS).
 *
 * These objects contain a reference to the alert (via the alert_id), the list
 * of DMS that were selected based on the alert area, the MULTI of the message
 * that was generated, and the name of the user that approved the alert (which
 * is null before approval or AUTO when approval mode is disabled).
 *
 * @author Gordon Parikh
 */
public interface IpawsDeployer extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "ipaws_deployer";

	/** Get the generation time of this deployer object */
	Date getGenTime();

	/** Get the Alert ID. */
	String getAlertId();

	/** Get the alert deployer that this replaces (if any).  Note that
	 *  updates to alerts trigger creation of a new deployer (not an
	 *  update). */
	String getReplaces();

	/** Get the alert start time */
	Date getAlertStart();

	/** Get the alert end time */
	Date getAlertEnd();

	/** Get the config used for this deployment */
	IpawsConfig getConfig();

	/** Set amount of time (in hours) to display a pre-alert message before
	 *  the alert becomes active.  First set from the config, then can be
	 *  changed for each alert.
	 */
	void setPreAlertTime(int hours);

	/** Get amount of time (in hours) to display a pre-alert message before
	 *  the alert becomes active.  First set from the config, then can be
	 *  changed for each alert.
	 */
	int getPreAlertTime();

	/** Set amount of time (in hours) to display a post-alert message after
	 *  an alert expires or an AllClear response type is sent via IPAWS.
	 *  First set from the config, then can be changed for each alert.
	 */
	void setPostAlertTime(int hours);

	/** Get amount of time (in hours) to display a post-alert message after
	 *  an alert expires or an AllClear response type is sent via IPAWS.
	 *  First set from the config, then can be changed for each alert.
	 */
	int getPostAlertTime();

	/** Get the list of DMS (represented as a string array) automatically
	 *  selected for deploying alert messages. */
	String[] getAutoDms();

	/** Get the list of DMS suggested automatically as optional DMS that
	 *  users may want to include for the deployment. */
	String[] getOptionalDms();

	/** Set the list of DMS requested to deploy */
	void setRequestedDms(String[] dms);

	/** Get the list of DMS actually deployed */
	String[] getDeployedDms();

	/** Get the MULTI generated automatically for deploying to DMS */
	String getAutoMulti();

	/** Set the MULTI requested to deploy to DMS */
	void setRequestedMulti(String m);

	/** Get the MULTI actually deployed to DMS */
	String getDeployedMulti();

	/** Set the message priority.
	 * @param p Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	void setMsgPriority(int p);

	/** Get the message priority.
	 * @return Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	int getMsgPriorty();

	/** Set the approving user. */
	void setApprovedBy(String u);

	/** Get the approving user. */
	String getApprovedBy();

	/** Get the approval time of this deployer object */
	Date getApprovedTime();

	/** Get alert state (ordinal of AlertState) */
	int getAlertState();

	/** Set alert state request (ordinal of AlertState) */
	void setAlertStateReq(int st);
}
