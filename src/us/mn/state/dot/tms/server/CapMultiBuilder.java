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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import us.mn.state.dot.tms.AlertInfoHelper;
import us.mn.state.dot.tms.utils.MultiBuilder;

/**
 * MULTI builder for CAP alert messages.
 *
 * @author Michael Janson
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class CapMultiBuilder extends MultiBuilder {

	/** Default time format string (hour and AM/PM) for CAP time tags. */
	static private final DateTimeFormatter DEFAULT_TIME_FMT =
		DateTimeFormatter.ofPattern("h a");

	/** Regex pattern for extracting time format string */
	static private final Pattern TMSUB = Pattern.compile("\\{([^}]*)\\}");

	/** Process time format substitution fields,
	 *  substituting in the time value provided. */
	static private String replaceTimeFmt(String tmplt, LocalDateTime dt) {
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

	/** CAP alert period */
	static public enum Period {
		BEFORE, // before alert start time
		DURING, // between start and end times
		AFTER;  // after end time
	}

	/** Period to generate */
	private final Period period;

	/** Alert start date */
	private final Date start_date;

	/** Alert end date */
	private final Date end_date;

	/** Create a new CAP MULTI builder */
	public CapMultiBuilder(Period p, Date sd, Date ed) {
		period = p;
		start_date = sd;
		end_date = ed;
	}

	/** Add a CAP time substitution field.
	 *  Text fields can include "{}" to automatically substitute in the
	 *  appropriate time (alert start or end time), with optional formatting
	 *  (using Java Date Format notation).
	 *  @param b_txt Before alert prepend text.
	 *  @param d_txt During-alert prepend text.
	 *  @param a_txt After-alert prepend text. */
	@Override
	public void addCapTime(String b_txt, String d_txt, String a_txt) {
		String tmplt = "";
		Date dt = new Date();
		switch (period) {
		case BEFORE:
			tmplt = b_txt;
			dt = start_date;
			break;
		case DURING:
			tmplt = d_txt;
			dt = end_date;
			break;
		case AFTER:
			tmplt = a_txt;
			dt = end_date;
			break;
		}

		// format any time strings in the text and add to the msg
		String s = replaceTimeFmt(tmplt, dt.toInstant().atZone(
			ZoneId.systemDefault()).toLocalDateTime());
		addSpan(s);
	}
}
