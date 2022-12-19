/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2022  Minnesota Department of Transportation
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
 * A message pattern is a partially or fully composed message for a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public interface MsgPattern extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "msg_pattern";

	/** Get the sign configuration */
	SignConfig getSignConfig();

	/** Set the sign configuration */
	void setSignConfig(SignConfig sc);

	/** Get the sign group associated with the pattern.
	 * @return Sign group; null for no group. */
	SignGroup getSignGroup();

	/** Set the sign group associated with the pattern.
	 * @param sg Sign group to associate; null for no group. */
	void setSignGroup(SignGroup sg);

	/** Get message combining value.
	 * @see us.mn.state.dot.tms.MsgCombining */
	int getMsgCombining();

	/** Set message combining value.
	 * @see us.mn.state.dot.tms.MsgCombining */
	void setMsgCombining(int mc);

	/** Get the message MULTI string.
	 * @return Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.utils.MultiString */
	String getMulti();

	/** Set the message MULTI string.
	 * @param multi Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.utils.MultiString */
	void setMulti(String multi);
}
