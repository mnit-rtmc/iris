/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
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

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = DMS.SONAR_TYPE;

	/** Maximum number of lines per text rectangle */
	int MAX_LINES = 4;

	/** Get the sign configuration */
	SignConfig getSignConfig();

	/** Get the message MULTI string.
	 * @return Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.utils.MultiString */
	String getMulti();

	/** Get the message owner.
	 *
	 * It contains 3 parts, separated by semicolons, for example
	 * "IRIS; operator+schedule; john.smith"
	 *  1. System ("IRIS")
	 *  2. Sources ("operator+schedule")
	 *  3. Name: user or action plan ("john.smith")
	 *
	 * @return Message owner
	 * @see us.mn.state.dot.tms.SignMsgSource
	 */
	String getMsgOwner();

	/** Get sticky flag */
	boolean getSticky();

	/** Get flash beacon flag */
	boolean getFlashBeacon();

	/** Get pixel service flag */
	boolean getPixelService();

	/** Get the message priority.
	 * @return Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.SignMsgPriority */
	int getMsgPriority();
}
