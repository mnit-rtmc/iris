/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignTextHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.MultiBuilder;

/**
 * MultiBuilder for replacing feed tags.
 *
 * @author Douglas Lau
 */
public class FeedCallback extends MultiBuilder {

	/** Check if msg feed verify is enabled */
	static private boolean isMsgFeedVerifyEnabled() {
		return SystemAttrEnum.MSG_FEED_VERIFY.getBoolean();
	}

	/** DMS ID */
	private final String did;

	/** Number of lines on sign */
	private final int n_lines;

	/** Sign group */
	private final SignGroup group;

	/** Matching feed message if any */
	private FeedMsg msg;

	/** Create a new feed callback */
	public FeedCallback(DMSImpl dms, SignGroup sg) {
		did = dms.getName();
		n_lines = DMSHelper.getLineCount(dms);
		group = sg;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		String ms = super.toString();
		if (msg == null)
			return ms;
		else if (ms.isEmpty())
			return getFeedString();
		else
			return "";
	}

	/** Get the feed message string */
	private String getFeedString() {
		if (!isMsgFeedVerifyEnabled() || isFeedMsgValid())
			return msg.getMulti().toString();
		else
			return "";
	}

	/** Test if the feed message is valid */
	private boolean isFeedMsgValid() {
		String[] lines = msg.getMulti().getLines(n_lines, "");
		for (int i = 0; i < lines.length; i++) {
			if (!isValidSignText((short) (i + 1), lines[i]))
				return false;
		}
		return true;
	}

	/** Check if a MULTI string is a valid sign text for the sign group */
	private boolean isValidSignText(short line, String ms) {
		return ms.isEmpty() ||
		       SignTextHelper.match(group, line, ms);
	}

	/** Add a feed tag */
	@Override
	public void addFeed(String fid) {
		msg = FeedBucket.getMessage(fid, did);
	}
}
