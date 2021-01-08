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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for IPAWS Alert Deployers.  Used on the client and server.
 *
 * @author Gordon Parikh
 */
public class IpawsDeployerHelper extends BaseHelper {

	/** Don't instantiate */
	private IpawsDeployerHelper() {
		assert false;
	}

	/** Lookup the alert deployer with the specified name */
	static public IpawsDeployer lookup(String name) {
		return (IpawsDeployer) namespace.lookupObject(
				IpawsDeployer.SONAR_TYPE, name);
	}

	/** Lookup an alert deployer object for the specified IpawsAlert
	 *  identifier.
	 *  @return The most recent active deployer for this alert.
	 */
	static public IpawsDeployer lookupDeployerFromAlert(String alertId) {
		// get list of deployers for this alert sorted newest to oldest
		ArrayList<IpawsDeployer> deployers =
				getDeployerList(alertId, null);
		if (deployers.size() > 0)
			return deployers.get(0);
		return null;
	}

	/** Lookup an alert deployer object for the specified IpawsAlert
	 *  identifier and config name.
	 *  @return the most recent active deployer for the alert and config.
	 */
	static public IpawsDeployer lookupDeployerFromAlert(String alertId,
		String configName)
	{
		ArrayList<IpawsDeployer> deployers =
			getDeployerList(alertId, configName);
		return (deployers.size() > 0) ?	deployers.get(0) : null;
	}

	/** Comparator to sort IpawsDeployers by genTime. */
	static private class DeployerGenTimeComparator
				implements Comparator<IpawsDeployer> {

		/** Multiplier to sort ascending (+1) or descending (-1) */
		private int ascMult = 1;

		public DeployerGenTimeComparator(boolean ascending) {
			if (!ascending)
				ascMult = -1;
		}

		@Override
		public int compare(IpawsDeployer o1, IpawsDeployer o2) {
			// get genTimes
			Date gt0 = o1.getGenTime();
			Date gt1 = o2.getGenTime();

			// check for nulls (just in case) - the non-null one
			// should be higher
			if (gt0 == null && gt1 != null)
				return 1;
			else if (gt0 != null && gt1 == null)
				return -1;
			else if (gt0 == null && gt1 == null)
				return 0;

			// no nulls - compare dates and apply multiplier
			return ascMult * gt0.compareTo(gt1);
		}
	}

	/** Get a list of all active deployers associated with the alert ID and
	 *  config provided.  If configName is null, all matching active alerts
	 *  are returned.  Objects are sorted from newest to oldest.
	 */
	static public ArrayList<IpawsDeployer> getDeployerList(String alertId,
		String configName)
	{
		return getDeployerList(alertId, configName, false);
	}

	/** Get a list of all "deployed" deployers associated with the alert ID
	 *  and config provided.  If configName is null, all matching active
	 *  alerts are returned.  Objects are sorted from newest to oldest
	 *  unless ascending is false.
	 */
	static public ArrayList<IpawsDeployer> getDeployerList(String alertId,
		String configName, boolean ascending)
	{
		ArrayList<IpawsDeployer> deployers =
			new ArrayList<IpawsDeployer>();

		// find all deployers associated with this alert
		Iterator<IpawsDeployer> it = iterator();
		while (it.hasNext()) {
			IpawsDeployer iad = it.next();
			if (iad.getAlertId().equals(alertId) && (configName == null
					|| configName.equals(iad.getConfig()))
					&& Boolean.TRUE.equals(iad.getDeployed()))
				deployers.add(iad);
		}

		// sort the list using a custom comparator
		deployers.sort(new DeployerGenTimeComparator(ascending));
		return deployers;
	}

	/** Get a list of deployers associated with the given deployer (i.e.
	 *  with the same alert ID and alert config replacing the given
	 *  deployer).  If allowPending is true, this includes any pending
	 *  alerts (i.e. with deployed = null), otherwise it only includes
	 *  active alerts (deployed = true).  Objects are sorted from newest to
	 *  oldest unless ascending is false.
	 */
	static public ArrayList<IpawsDeployer> getDeployerList(
			IpawsDeployer iadOld, boolean allowPending,
			boolean ascending) {
		ArrayList<IpawsDeployer> deployers =
				new ArrayList<IpawsDeployer>();

		// find all deployers associated with this alert
		Iterator<IpawsDeployer> it = iterator();
		while (it.hasNext()) {
			IpawsDeployer iad = it.next();
			if (iad.getAlertId().equals(iadOld.getAlertId())
					&& iad.getConfig().equals(iadOld.getConfig())
					&& iadOld.getName().equals(iad.getReplaces())
					&& (Boolean.TRUE.equals(iad.getDeployed())
						|| (iad.getDeployed() == null && allowPending)))
				deployers.add(iad);
		}

		// sort the list using a custom comparator
		deployers.sort(new DeployerGenTimeComparator(ascending));
		return deployers;
	}

	/** Get an IpawsDeployer object iterator */
	static public Iterator<IpawsDeployer> iterator() {
		return new IteratorWrapper<IpawsDeployer>(namespace.iterator(
				IpawsDeployer.SONAR_TYPE));
	}

	/** Default time format string (hour and AM/PM) for CAP time tags. */
	private final static DateTimeFormatter DEFAULT_TIME_FMT =
			DateTimeFormatter.ofPattern("h a");

	/** Regex pattern for extracting time format string */
	private final static Pattern TMSUB = Pattern.compile("\\{([^}]*)\\}");

	/** Process time format substitution fields, substituting in the time
	 *  value provided.
	 */
	public static String replaceTimeFmt(String tmplt, LocalDateTime dt) {
		// use regex to find match groups in curly braces
		Matcher m = TMSUB.matcher(tmplt);
		String str = tmplt;
		while (m.find()) {
			String tmfmt = m.group(1);
			String subst;
			DateTimeFormatter dtFmt;

			// get the full string for replacement and a
			// DateTimeFormatter
			if (tmfmt.trim().isEmpty()) {
				dtFmt = DEFAULT_TIME_FMT;
				subst = "{}";
			} else {
				dtFmt = DateTimeFormatter.ofPattern(tmfmt);
				subst = "{" + tmfmt + "}";
			}

			// format the time string and swap it in
			String tmstr = dt.format(dtFmt);
			str = str.replace(subst, tmstr);
		}
		return str;
	}

	/** Get the time since the alert deployer was generated in seconds. */
	public static long getTimeSinceGenerated(IpawsDeployer iad) {
		LocalDateTime at = iad.getGenTime().toInstant().atZone(
				ZoneId.systemDefault()).toLocalDateTime();
		return Duration.between(at, LocalDateTime.now()).getSeconds();
	}
}
