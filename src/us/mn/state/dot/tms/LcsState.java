/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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

/**
 * An LCS state represents a lane/indication combination for an LCS array.
 *
 * @author Douglas Lau
 */
public interface LcsState extends ControllerIO {

	/** SONAR type name */
	String SONAR_TYPE = "lcs_state";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = Lcs.SONAR_TYPE;

	/** Get the LCS array */
	Lcs getLcs();

	/** Set the lane (starting from right lane as 1) */
	void setLane(int ln);

	/** Get the lane (starting from right lane as 1) */
	int getLane();

	/** Set the indication (ordinal of LcsIndication) */
	void setIndication(int i);

	/** Get the indication (ordinal of LcsIndication) */
	int getIndication();

	/** Set the message pattern */
	void setMsgPattern(MsgPattern pat);

	/** Get the message pattern */
	MsgPattern getMsgPattern();

	/** Set the message number */
	void setMsgNum(Integer n);

	/** Get the message number */
	Integer getMsgNum();
}
