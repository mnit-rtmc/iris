/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.aws;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.StringTokenizer;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.SString;

/**
 * Container for AWS messages.
 * @author Michael Darter
 */
public class AwsMsgs implements Serializable
{
	/** messages */
	LinkedList<AwsMsg> m_msgs = null;

	/** constructor */
	public AwsMsgs(byte[] bmsgs) {
		Log.finest("AwsMsgs.AwsMsgs() called.");
		this.parse(bmsgs);
	}

	/** Parse a byte array of messages and add each dms 
	 *  message to the container. */
	private void parse(byte[] argmsgs) {
		m_msgs = new LinkedList<AwsMsg>();

		// cycle through each line, which is terminated by '\n'
		String msgs = SString.byteArrayToString(argmsgs);
		StringTokenizer lineTok = new StringTokenizer(msgs, "\n");

		while(lineTok.hasMoreTokens()) {
			String line = lineTok.nextToken();
			AwsMsg amsmsg = new AwsMsg();
			amsmsg.parse(line);
			if(amsmsg.getValid())
				m_msgs.add(amsmsg);
		}
	}

	/** activate the messages */
	public void activate() {
		if(m_msgs == null)
			return;
		Log.finest("=====Starting activating AWS messages");
		for(AwsMsg m: m_msgs) {
			// get the iris DMS id, e.g. "V30"
			String id = m.getIrisDmsId();
			DMSImpl dms = null; // FIXME: was: TMSObjectImpl.lookupDms(id);
			if(dms != null)
				m.activate(dms);
		}
		Log.finest("=====End activating AWS messages");
	}
}
