/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
 * Road interface
 *
 * @author Douglas Lau
 */
public interface Road extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "road";

	/** Set the abbreviated name */
	void setAbbrev(String a);

	/** Get the abbreviated name */
	String getAbbrev();

	/** Set the roadway class */
	void setRClass(short c);

	/** Get the roadway class */
	short getRClass();

	/** Set direction */
	void setDirection(short d);

	/** Get direction */
	short getDirection();

	/** Set alternate direction */
	void setAltDir(short a);

	/** Get alternate direction */
	short getAltDir();
}
