/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2013  Minnesota Department of Transportation
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
 * Sign text contains the properties of a single line MULTI string for display
 * on a dynamic message sign (DMS).
 *
 * @author Douglas Lau
 */
public interface SignText extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "sign_text";

	/** Get the sign group */
	SignGroup getSignGroup();

	/** Set the line */
	void setLine(short l);

	/** Get the line */
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
