/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
 * A lane-use MULTI is an association between lane-use indication and a
 * quick message MULTI string.
 *
 * @author Douglas Lau
 */
public interface LaneUseMulti extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "lane_use_multi";

	/** Set the indication (ordinal of LaneUseIndication) */
	void setIndication(int i);

	/** Get the indication (ordinal of LaneUseIndication) */
	int getIndication();

	/** Set the message number */
	void setMsgNum(Integer n);

	/** Get the message number */
	Integer getMsgNum();

	/** Set the indication sign width */
	void setWidth(int w);

	/** Get the indication sign width */
	int getWidth();

	/** Set the indication sign height */
	void setHeight(int h);

	/** Get the indication sign height */
	int getHeight();

	/** Set the quick message */
	void setQuickMessage(QuickMessage qm);

	/** Get the quick message */
	QuickMessage getQuickMessage();
}
