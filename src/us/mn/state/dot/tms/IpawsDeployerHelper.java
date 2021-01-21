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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
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

	/** Get an IpawsDeployer object iterator */
	static public Iterator<IpawsDeployer> iterator() {
		return new IteratorWrapper<IpawsDeployer>(namespace.iterator(
			IpawsDeployer.SONAR_TYPE));
	}

	/** Default time format string (hour and AM/PM) for CAP time tags. */
	static private final DateTimeFormatter DEFAULT_TIME_FMT =
		DateTimeFormatter.ofPattern("h a");

	/** Regex pattern for extracting time format string */
	static private final Pattern TMSUB = Pattern.compile("\\{([^}]*)\\}");

	/** Process time format substitution fields, substituting in the time
	 *  value provided.
	 */
	static public String replaceTimeFmt(String tmplt, LocalDateTime dt) {
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
}
