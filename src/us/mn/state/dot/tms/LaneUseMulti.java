/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
 * message pattern MULTI.
 *
 * @author Douglas Lau
 */
public interface LaneUseMulti extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "lane_use_multi";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = LCS.SONAR_TYPE;

	/** Set the indication (ordinal of LaneUseIndication) */
	void setIndication(int i);

	/** Get the indication (ordinal of LaneUseIndication) */
	int getIndication();

	/** Set the message number */
	void setMsgNum(Integer n);

	/** Get the message number */
	Integer getMsgNum();

	/** Set the message pattern */
	void setMsgPattern(MsgPattern pat);

	/** Get the message pattern */
	MsgPattern getMsgPattern();

	/** Set the DMS hashtag */
	void setDmsHashtag(String ht);

	/** Get the DMS hashtag */
	String getDmsHashtag();
}
