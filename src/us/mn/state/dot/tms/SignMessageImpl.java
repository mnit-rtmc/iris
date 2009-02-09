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
package us.mn.state.dot.tms;

import java.io.IOException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.SonarException;

/**
 * A sign message represents a message which can be displayed on a dynamic
 * message sign (DMS). It contains the text associated with the message and a
 * bitmap for each page of the message.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignMessageImpl implements SignMessage {

	/** Last allocated system message ID */
	static protected int last_id = 0;

	/** Create a unique sign message name */
	static protected synchronized String createUniqueName() {
		last_id++;
		// Check if the ID has rolled over to negative numbers
		if(last_id < 0)
			last_id = 0;
		return "system_" + last_id;
	}

	/** Sign message name */
	protected final String name;

	/** Get the sign message */
	public String getName() {
		return name;
	}

	/** Create a new sign message (by SONAR clients) */
	public SignMessageImpl(String n) {
		name = n;
	}

	/** Create a new sign message (by IRIS) */
	public SignMessageImpl(String m, String[] b, DMSMessagePriority p)
		throws SonarException
	{
		name = createUniqueName();
		multi = m;
		bitmaps = b;
		duration = null;
		activationPriority = p.ordinal();
		if(isBlank())
			runTimePriority = DMSMessagePriority.BLANK.ordinal();
		else
			runTimePriority = p.ordinal();
		owner = null;
		MainServer.server.createObject(this);
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Destroy an object */
	public void destroy() {
		// Not stored in database
	}

	/** Check if the sign message is blank */
	public boolean isBlank() {
		return isMultiBlank() && isBitmapBlank();
	}

	/** Check if the MULTI string is blank */
	protected boolean isMultiBlank() {
		return new MultiString(multi).isBlank();
	}

	/** Check if the bitmap is blank */
	protected boolean isBitmapBlank() {
		try {
			for(String bmap: bitmaps) {
				for(byte b: Base64.decode(bmap)) {
					if(b != 0)
						return false;
				}
			}
			return true;
		}
		catch(IOException e) {
			return false;
		}
	}

	/** Message MULTI string, contains message text for all pages */
	protected String multi;

	/** Get the message MULTI string.
	 * @return Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.MultiString */
	public String getMulti() {
		return multi;
	}

	/** Array of bitmaps for each page (Base64-encoded) */
	protected String[] bitmaps;

	/** Get the bitmaps for all pages of the message.
	 * @return Array of Base64-encoded bitmaps, one for each page.
	 * @see us.mn.state.dot.tms.Base64 */
	public String[] getBitmaps() {
		return bitmaps;
	}

	/** Message deploy time */
	protected long deployTime = System.currentTimeMillis();

	/** Set the message deploy time */
	public void setDeployTime(long t) {
		deployTime = t;
		notifyAttribute("deployTime");
	}

	/** Get the message deploy time.
	 * @return Time message was deployed (ms since epoch).
	 * @see java.lang.System.currentTimeMillis */
	public long getDeployTime() {
		return deployTime;
	}

	/** Duration of this message (minutes) */
	protected Integer duration;

	/** Set the message duration */
	public void setDuration(Integer d) {
		duration = d;
		notifyAttribute("duration");
	}

	/** Get the message duration.
	 * @return Duration in minutes; null means indefinite. */
	public Integer getDuration() {
		return duration;
	}

	/** Message activation priority */
	protected int activationPriority;

	/** Set the message activation priority */
	public void setActivationPriority(int p) {
		activationPriority = p;
		notifyAttribute("activationPriority");
	}

	/** Get the message activation priority.
	 * @return Activation priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
	public int getActivationPriority() {
		return activationPriority;
	}

	/** Message run-time priority */
	protected int runTimePriority;

	/** Set the message run-time priority */
	public void setRunTimePriority(int p) {
		runTimePriority = p;
		notifyAttribute("runTimePriority");
	}

	/** Get the message run-time priority.
	 * @return Run-time priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
	public int getRunTimePriority() {
		return runTimePriority;
	}

	/** Message owner */
	protected User owner;

	/** Set the message owner */
	public void setOwner(User o) {
		owner = o;
		notifyAttribute("owner");
	}

	/** Get the message owner.
	 * @return User who deployed the message. */
	public User getOwner() {
		return owner;
	}

	/** Notify SONAR clients of a change to an attribute */
	protected void notifyAttribute(String aname) {
		if(MainServer.server != null)
			MainServer.server.setAttribute(this, aname);
	}
}
