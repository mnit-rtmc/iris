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

package us.mn.state.dot.tms.comm.caws;

import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.TMSImpl;
import us.mn.state.dot.tms.utils.SString;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * CAWS D10CmsMsgs. This is a collection of CMS messages.
 *
 * @author Michael Darter
 */
public class D10CmsMsgs implements Serializable
{
	// fields
	LinkedList<D10CmsMsg> m_msgs = null;

	/** constructor */
	public D10CmsMsgs(byte[] bmsgs) {
		System.err.println("D10CmsMsgs.D10CmsMsgs() called.");
		this.parse(bmsgs);
	}

	/**
	 * parse a byte array of messages and add each cms message to the container.
	 */
	private void parse(byte[] argmsgs) {
		m_msgs = new LinkedList<D10CmsMsg>();

		// cycle through each line, which is terminated by '\n'
		String msgs = SString.byteArrayToString(argmsgs);
		StringTokenizer lineTok = new StringTokenizer(msgs, "\n");

		while(lineTok.hasMoreTokens()) {
			String line = lineTok.nextToken();
			D10CmsMsg cmsmsg = new D10CmsMsg(line);
			this.m_msgs.add(cmsmsg);
		}
	}

	/** activate the messages */
	public void activate() {
		// sanity check
		if(m_msgs == null)
			return;

		// activate each msg
		for(D10CmsMsg m: m_msgs) {
			// get the iris cms id, e.g. "V30"
			String irisCmsId = m.getIrisCmsId();
			DMSImpl dms = TMSImpl.lookupDms(irisCmsId);
			if(dms != null)
				m.activate(dms);
		}
	}
}
