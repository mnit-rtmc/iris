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
package us.mn.state.dot.tms.server;

import java.util.Calendar;
import java.util.Iterator;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Job to process IPAWS alerts.  Alerts are written to the database by the
 * ipaws CommLink, which handles polling the IPAWS-OPEN server, parsing CAP
 * XMLs, and storing all alerts in the database.
 *
 * This job processes these alerts, performing filtering based on the contents
 * of the alert (including field values and geographic reach).  Irrelevant
 * alerts are marked for purging to be (optionally) deleted by a flush job
 * (partially implemented).
 *
 * This job also standardizes geographic data from the alerts and handles DMS
 * selection, message creation, client notification, and in some modes posting
 * alert messages to DMS.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class IpawsProcJob extends Job {

	/** Send an IPAWS email alert */
	static public void sendEmailAlert(String msg) {
		String recip = SystemAttrEnum.EMAIL_RECIPIENT_AWS.getString();
		EmailHandler.sendEmail(EMAIL_SUB, msg, recip);
	}

	/** Email subject for alert errors */
	static private final String EMAIL_SUB =
		"Error in IRIS IPAWS Processing System";

	/** Email message for alert errors */
	static private final String EMAIL_MSG =
		"Error encountered in IPAWS alert processing system.  " +
		"Check the server logs for details.";

	/** IPAWS Debug Log */
	static private final DebugLog IPAWS_LOG = new DebugLog("ipaws");

	/** Table containing NWS Forecast Zone Geometries.  This can be obtained
	 *  / updated from the NWS by going to this website and importing the
	 *  shapefile into PostGIS: https://www.weather.gov/gis/PublicZones.
	 */
	static private final String GEOMETRY_TABLE = "iris.nws_zones";

	/** Seconds to offset this job from the start of interval.
	 *  Alerts will generally be polled at the top of each minute, so we
	 *  will run this job 30 seconds after. */
	static private final int OFFSET_SECS = 30;

	/** Log an IPAWS message */
	static public void log(String msg) {
		if (IPAWS_LOG.isOpen())
			IPAWS_LOG.log(msg);
	}

	/** Create a new job to process IPAWS alerts in the database. */
	public IpawsProcJob() {
		super(Calendar.MINUTE, 1, Calendar.SECOND, OFFSET_SECS);
	}

	/** Process IPAWS alerts in the database */
	@Override
	public void perform() {
		processAllAlerts();
		processAllDeployers();
	}

	/** Process all alerts */
	private void processAllAlerts() {
		Iterator<IpawsAlertImpl> it = IpawsAlertImpl.iterator();
		while (it.hasNext()) {
			processAlert(it.next());
		}
	}

	/** Process an IPAWS alert for action needed */
	private void processAlert(IpawsAlertImpl ia) {
		try {
			ia.processAlert();
		}
		catch (Exception e) {
			e.printStackTrace();
			sendEmailAlert(EMAIL_MSG);
		}
	}

	/** Process all alert deployers */
	private void processAllDeployers() {
		Iterator<IpawsDeployerImpl> it = IpawsDeployerImpl.iterator();
		while (it.hasNext()) {
			processDeployer(it.next());
		}
	}

	/** Process an IPAWS deployer for action needed */
	private void processDeployer(IpawsDeployerImpl dp) {
		try {
			dp.checkStateChange();
		}
		catch (Exception e) {
			e.printStackTrace();
			sendEmailAlert(EMAIL_MSG);
		}
	}
}
