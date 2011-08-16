/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2011  Minnesota Department of Transportation
 * Copyright (C) 2008-2010  AHMCT, University of California, Davis
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
package us.mn.state.dot.tms.server.comm.msgfeed;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.DMSImpl;

/**
 * Feed sign message.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class FeedMsg {

	/** Message priority for feed messages */
	static private final DMSMessagePriority PRIO = DMSMessagePriority.PSA;

	/** DMS to send message */
	private final DMSImpl dms;

	/** MULTI string */
	private final MultiString multi;

	/** Create a new feed message */
	public FeedMsg(String line) {
		String[] msg = line.split("\t", 2);
		dms = parseDms(msg[0]);
		if(msg.length > 1)
			multi = new MultiString(msg[1]);
		else
			multi = new MultiString();
	}

	/** Return the DMS or null if it doesn't exist */
	private DMSImpl parseDms(String txt) {
		DMS dms = DMSHelper.lookup(txt.trim());
		if(dms instanceof DMSImpl)
			return (DMSImpl)dms;
		else
			return null;
	}

	/** Check if the feed message is valid */
	public boolean isValid() {
		return dms != null && multi.isValid();
	}

	/** Activate the message */
	public void activate() {
		if(shouldSendMessage())
			sendMessage();
	}

	/** Decide if the message should be sent to a DMS.
	 * @return true to send the message. */
	protected boolean shouldSendMessage() {
		/* FIXME: check that DMS exists in sign group */
		/* FIXME: check that message exists in sign group messages */
		return (!isMessageEquivalent()) &&
		       dms.shouldActivate(PRIO, multi, false);
	}

	/** Is the current sign message equivalent to the specified MULTI? */
	protected boolean isMessageEquivalent() {
		return multi.isEquivalent(dms.getMessageCurrent().getMulti());
	}

	/** Send the message to the DMS */
	protected void sendMessage() {
		try {
			dms.doSetMessageNext(createMessage(), null);
		}
		catch(TMSException e) {
			e.printStackTrace();
		}
	}

	/** Create a SignMessage version of this message.
	 * @return A SignMessage that contains the text of the message and
	 *         rendered bitmap(s). */
	private SignMessage createMessage() {
		DMSMessagePriority rp = (multi.isBlank() ? 
			DMSMessagePriority.BLANK : PRIO);
		return dms.createMessage(multi.toString(), PRIO, rp, null);
	}
}
