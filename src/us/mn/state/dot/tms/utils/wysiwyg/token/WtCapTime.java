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
package us.mn.state.dot.tms.utils.wysiwyg.token;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** CAP time substitution field token for WYSIWYG editor.
 * 
 * @author Gordon Parikh - SRF Consulting
 *
 */
public class WtCapTime extends Wt_IrisToken {

	String f_txt;
	String a_txt;
	String p_txt;
	
	/** Date object that should take up the max number of characters */
	private static final LocalDateTime BIG_DATE =
			LocalDateTime.of(2020, 12, 31, 23, 59, 59);
	
	/** Default time format string (hour and AM/PM). TODO may want to put this
	 *  somewhere else...
	 */
	private final static DateTimeFormatter DEFAULT_TIME_FMT =
			DateTimeFormatter.ofPattern("h a");
	
	/** Regex pattern for extracting time format string */
	private final static Pattern TMSUB = Pattern.compile("\\{([^}]*)\\}");
	
	public WtCapTime(String f_txt, String a_txt, String p_txt) {
		super(WTokenType.capTime, "[captime");
		this.f_txt = f_txt;
		this.a_txt = a_txt;
		this.p_txt = p_txt;
		updateString();
	}

	/** Get pre-alert text (when alert is in the future) */
	public String getFutureText() {
		return f_txt;
	}
	
	/** Get alert-active text (when alert is currently active) */
	public String getActiveText() {
		return a_txt;
	}
	
	/** Get post-alert text (when alert has expired) */
	public String getPastText() {
		return p_txt;
	}
	
	/** Process time format substitution fields, substituting in a large time
	 *  value (i.e. one that will take up a lot of characters).
	 */
	private static String replaceTimeFmt(String tmplt) {
		// use regex to find match groups in curly braces
		Matcher m = TMSUB.matcher(tmplt);
		String str = tmplt;
		while (m.find()) {
			String tmfmt = m.group(1);
			String subst;
			DateTimeFormatter dtFmt;
			
			// get the full string for replacement and a DateTimeFormatter
			if (tmfmt.trim().isEmpty()) {
				dtFmt = DEFAULT_TIME_FMT;
				subst = "{}";
			} else {
				dtFmt = DateTimeFormatter.ofPattern(tmfmt);
				subst = "{" + tmfmt + "}";
			}
			
			// format the time string and swap it in
			String tmstr = BIG_DATE.format(dtFmt);
			str = str.replace(subst, tmstr);
		}
		return str;
	}
	
	/** Get width of WYSIWYG box
	 * @param chsp Character spacing (null = use font default)
	 */
	@Override
	public Integer getBoxWidth(Integer chsp) {
		// go through different text options to determine potential lengths
		// and return the max width
		int maxWidth = 0;
		for (String tmplt: new String[] {f_txt, a_txt, p_txt}) {
			// replace any time format strings in the text and get the width
			// of the resulting text
			String str = replaceTimeFmt(tmplt);
			int w = wfont.getTextWidth(chsp, str);
			if (w > maxWidth)
				maxWidth = w;
		}
		return maxWidth;
	}
	
	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.addCapTime(f_txt, a_txt, p_txt);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		sb.append(f_txt);
		sb.append(',');
		sb.append(a_txt);
		sb.append(',');
		sb.append(p_txt);
	}
	
}
