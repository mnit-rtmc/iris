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

import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignTextHelper;

/**
 * MultiString for replacing feed tags
 *
 * @author Douglas Lau
 */
public class FeedCallback extends MultiString {

	/** DMS ID */
	private final String did;

	/** Sign group */
	private final SignGroup group;

	/** Matching feed message if any */
	private FeedMsg msg;

	/** Create a new feed callback */
	public FeedCallback(DMSImpl dms, SignGroup sg) {
		did = dms.getName();
		group = sg;
	}

	/** Add a feed tag */
	public void addFeed(String fid) {
		msg = FeedBucket.getMessage(fid, did);
	}

	/** Get a string representation */
	public String toString() {
		if(msg == null)
			return super.toString();
		else if(multi.length() == 0)
			return getFeedString();
		else
			return "";
	}

	/** Get the feed message string */
	private String getFeedString() {
		String[] lines = msg.getMulti().getText();
		for(int i = 0; i < lines.length; i++) {
			if(!SignTextHelper.match(group, (short)(i+1), lines[i]))
				return null;
		}
		return msg.getMulti().toString();
	}
}
