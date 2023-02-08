/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
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
 * Action for sending a message to a DMS sign group triggered by an action plan.
 *
 * @author Douglas Lau
 */
public interface DmsAction extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "dms_action";

	/** Get the action plan */
	ActionPlan getActionPlan();

	/** Set the phase to perform action */
	void setPhase(PlanPhase p);

	/** Get the phase to perform action */
	PlanPhase getPhase();

	/** Set the DMS hashtag */
	void setDmsHashtag(String ht);

	/** Get the DMS hashtag */
	String getDmsHashtag();

	/** Set the message pattern */
	void setMsgPattern(MsgPattern pat);

	/** Get the message pattern */
	MsgPattern getMsgPattern();

	/** Set flash beacon flag */
	void setFlashBeacon(boolean fb);

	/** Get flash beacon flag */
	boolean getFlashBeacon();

	/** Set the message priority.
	 * @param p Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	void setMsgPriority(int p);

	/** Get the message priority.
	 * @return Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	int getMsgPriority();
}
