/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.MultiString;

/**
 * Feed sign message.
 *
 * @author Douglas Lau
 */
public class FeedMsg {

	/** Feed name */
	private final String feed;

	/** Get the feed name */
	public String getFeed() {
		return feed;
	}

	/** DMS to send message */
	private final String dms;

	/** Get the DMS */
	public String getDms() {
		return dms;
	}

	/** MULTI string */
	private final MultiString multi;

	/** Get the MULTI string */
	public MultiString getMulti() {
		return multi;
	}

	/** Expire time */
	private final Date expire;

	/** Create a new feed message */
	public FeedMsg(String fd, String line) {
		feed = fd;
		String[] msg = line.split("\t", 3);
		dms = parseDms(msg[0]);
		multi = (msg.length > 1) ? new MultiString(msg[1]) : null;
		expire = (msg.length > 2) ? parseTime(msg[2]) : null;
	}

	/** Return the DMS name or null if it doesn't exist */
	private String parseDms(String txt) {
		DMS dms = DMSHelper.lookup(txt.trim());
		if(dms != null)
			return dms.getName();
		else
			return null;
	}

	/** Parse a time stamp */
	private Date parseTime(String time) {
		SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ssZ", Locale.US);
		try {
			return sdf.parse(time);
		}
		catch(ParseException e) {
			return null;
		}
	}

	/** Get a string representation of the feed message */
	public String toString() {
		return "feed: " + feed +  ", dms: " + dms + ", multi: " +
			multi + ", expire: " + expire;
	}

	/** Check if the feed message is valid */
	public boolean isValid() {
		return dms != null && isMultiValid() && !hasExpired();
	}

	/** Check if the multi string is valid */
	private boolean isMultiValid() {
		return multi != null && multi.isValid();
	}

	/** Check if the feed message has expired */
	public boolean hasExpired() {
		return expire == null || expire.before(new Date());
	}
}
