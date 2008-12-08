/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.User;

/**
 * A sign message represents a message which can be displayed on a dynamic
 * message sign (DMS). It contains the text associated with the message and a
 * bitmap for each page of the message.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public interface SignMessage extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "sign_message";

	/** Get the message MULTI string.
	 * @return Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.MultiString */
	String getMulti();

	/** Get the bitmaps for all pages of the message.
	 * @return Array of Base64-encoded bitmaps, one for each page.
	 * @see us.mn.state.dot.tms.Base64 */
	String[] getBitmaps();

	/** Get the message deploy time.
	 * @return Time message was deployed (ms since epoch).
	 * @see java.lang.System.currentTimeMillis */
	long getDeployTime();

	/** Get the message duration.
	 * @return Duration in minutes; null means indefinite. */
	Integer getDuration();

	/** Get the message activation priority.
	 * @return Activation priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
	int getActivationPriority();

	/** Get the message run-time priority.
	 * @return Run-time priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
	int getRunTimePriority();

	/** Get the message owner.
	 * @return User who deployed the message. */
	User getOwner();
}
