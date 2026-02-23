/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2026  Minnesota Department of Transportation
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
 * A message line contains the properties of a single line MULTI string for
 * filling in a message pattern.
 *
 * @author Douglas Lau
 */
public interface MsgLine extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "msg_line";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = DMS.SONAR_TYPE;

	/** Maximum length of MULTI string */
	int MAX_LEN_MULTI = 64;

	/** Get the message pattern */
	MsgPattern getMsgPattern();

	/** Set the line number */
	void setLine(short l);

	/** Get the line number */
	short getLine();

	/** Set the MULTI string */
	void setMulti(String m);

	/** Get the MULTI string */
	String getMulti();

	/** Set the rank */
	void setRank(short r);

	/** Get the rank */
	short getRank();
}
