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

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import us.mn.state.dot.tms.CapResponse;
import us.mn.state.dot.tms.CapResponseEnum;
import us.mn.state.dot.tms.CapResponseHelper;
import us.mn.state.dot.tms.CapUrgency;
import us.mn.state.dot.tms.CapUrgencyHelper;
import us.mn.state.dot.tms.IpawsDeployerHelper;
import us.mn.state.dot.tms.utils.MultiBuilder;

/**
 * MULTI builder for CAP alert messages.
 *
 * @author Michael Janson
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class CapMultiBuilder extends MultiBuilder {

	private final IpawsAlertImpl alert;

	/** Create a new CAP MULTI builder */
	public CapMultiBuilder(IpawsAlertImpl a) {
		alert = a;
	}

	/** Add an IPAWS CAP time substitution field.  Text fields can include
	 *  "{}" to automatically substitute in the appropriate time (alert
	 *  start or end time), with optional formatting (using Java Date Format
	 *  notation).
	 *  @param f_txt Pre-alert text.
	 *  @param a_txt Alert-active prepend text.
	 *  @param p_txt Post-alert prepend text.
	 */
	@Override
	public void addCapTime(String f_txt, String a_txt, String p_txt) {
		Date alert_start = alert.getAlertStart();
		Date alert_end = alert.getExpirationDate();
		Date now = new Date();
		String tmplt;
		Date dt;
		if (now.before(alert_start)) {
			// alert hasn't started yet
			tmplt = f_txt;
			dt = alert_start;
		} else if (now.before(alert_end)) {
			// alert is currently active
			tmplt = a_txt;
			dt = alert_end;
		} else {
			// alert has expired
			tmplt = p_txt;
			dt = alert_end;
		}

		// format any time strings in the text and add to the msg
		String s = IpawsDeployerHelper.replaceTimeFmt(tmplt,
			dt.toInstant().atZone(ZoneId.systemDefault())
			.toLocalDateTime());
		addSpan(s);
	}

	/** Add an IPAWS CAP response type substitution field.
	 *  @param rtypes Optional list of response types to consider.
	 */
	@Override
	public void addCapResponse(String[] rtypes) {
		// make a HashSet of the allowed response types
		HashSet<String> rtSet = new HashSet<String>(
			Arrays.asList(rtypes));

		// check the response types in the alert to see if we should
		// substitute anything, taking the highest priority one
		CapResponseEnum maxRT = CapResponseEnum.NONE;
		CapResponse rtSub = null;
		for (String rt: alert.getResponseTypes()) {
			if (rtSet.contains(rt)) {
				// make sure we have a matching substitution
				// value too
				CapResponse crt = CapResponseHelper.lookupFor(
					alert.getEvent(), rt);
				if (crt != null) {
					CapResponseEnum crte =
						CapResponseEnum.fromValue(rt);
					if (crte.ordinal() > maxRT.ordinal()) {
						maxRT = crte;
						rtSub = crt;
					}
				}
			}
		}

		// if we had a match add the MULTI, otherwise leave it blank
		addSpan(rtSub != null ? rtSub.getMulti() : "");
	}

	/** Add an IPAWS CAP urgency substitution field.
	 *  @param uvals Optional list of urgency values to consider.
	 */
	@Override
	public void addCapUrgency(String[] uvals) {
		HashSet<String> urgSet = new HashSet<String>(
			Arrays.asList(uvals));

		// check the urgency value in the alert to see if we should
		// substitute anything
		String urg = alert.getUrgency();
		String multi = "";
		if (urgSet.contains(urg)) {
			CapUrgency subst = CapUrgencyHelper.lookupFor(
				alert.getEvent(), urg);
			if (subst != null)
				multi = subst.getMulti();
		}
		addSpan(multi);
	}
}
