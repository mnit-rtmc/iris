/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2022  Minnesota Department of Transportation
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

/**
 * A sign message represents a message which can be displayed on a dynamic
 * message sign (DMS).  All values in these messages are *immutable* -- if any
 * changes are needed, a new sign message must be created.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public interface SignMessage extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "sign_message";

	/** Maximum number of lines per page */
	int MAX_LINES = 4;

	/** Maximum allowed pages for a message */
	int MAX_PAGES = 6;

	/** Get the sign configuration */
	SignConfig getSignConfig();

	/** Get the associated incident (original name) */
	String getIncident();

	/** Get the message MULTI string.
	 * @return Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.utils.MultiString */
	String getMulti();

	/** Get beacon enabled flag */
	boolean getBeaconEnabled();

	/** Get message combining value.
	 * @see us.mn.state.dot.tms.MsgCombining */
	int getMsgCombining();

	/** Get the message priority.
	 * @return Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	int getMsgPriority();

	/** Get the sign message source value.
	 * @return Sign message source.
	 * @see us.mn.state.dot.tms.SignMsgSource */
	int getSource();

	/** Get the message owner.
	 * @return Message owner (User or action plan name). */
	String getOwner();

	/** Get the message duration.
	 * @return Duration in minutes; null means indefinite. */
	Integer getDuration();
}
