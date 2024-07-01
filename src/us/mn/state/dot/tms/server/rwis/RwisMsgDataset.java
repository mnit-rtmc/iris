/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  SRF Consulting Group
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

package us.mn.state.dot.tms.server.rwis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;

/** An temporary indexed collection of
 *  RWIS MsgPattern(s).
 *
 * Note:  If more than one message name contains
 * the same RWIS hashtag and priority, then only
 * one of those messages will be used and the
 * dupError() method will return true.
 * 
 * @author John L. Stanley - SRF Consulting
 **/
public class RwisMsgDataset {

	HashMap<String, MsgPattern> msgMap = new HashMap<String, MsgPattern>();

	boolean bDupError = false;
	
	/** Create RwisMsgDataset and load messages */
	public RwisMsgDataset() {
		Pattern pattern = Pattern.compile("^(RWIS[\\d\\w]*)_(\\d+)(_.*){0,1}");

		Iterator<MsgPattern> it = MsgPatternHelper.iterator();
		while (it.hasNext()) {
			MsgPattern msg = it.next();
			String msgName = msg.getName();
			Matcher m = pattern.matcher(msgName);
			if (!m.find() || (m.groupCount() < 2))
				continue;
			String key = m.group(1) + "_" + m.group(2);
			if (msgMap.containsKey(key)) {
				bDupError = true;
				continue;
			}
			msgMap.put(key, msg);
		}
	}

	/** Returns true if more than one message exists
	 *  for one or more hashtag/priority combination.*/
	public boolean dupError() {
		return bDupError;
	}
	
	/** Returns the MsgPattern that matches the
	 *  given hashtag and priority.
	 *  Returns null if no such message exists. */
	public MsgPattern get(String hashtag, Integer priority) {
		String key = hashtag + "_" + priority;
		return msgMap.get(key);
	}
}
