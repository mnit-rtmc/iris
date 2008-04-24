/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

	/** Undefined road class / direction */
	short NONE = 0;

	/** Residential (A) roadway class */
	short RESIDENTIAL = 1;

	/** Business (B) roadway class */
	short BUSINESS = 2;

	/** Collector (C) roadway class */
	short COLLECTOR = 3;

	/** Arterial (D) roadway class */
	short ARTERIAL = 4;

	/** Expressway (E) roadway class */
	short EXPRESSWAY = 5;

	/** Freeway (F) roadway class */
	short FREEWAY = 6;

	/** Collector-Distributor roadway class */
	short CD_ROAD = 7;

	/** Roadway classes */
	String[] R_CLASS = {
		" ", "Residential", "Business", "Collector", "Arterial",
		"Expressway", "Freeway", "CD Road"
	};

	/** North direction */
	short NORTH = 1;

	/** South direction */
	short SOUTH = 2;

	/** East direction */
	short EAST = 3;

	/** West direction */
	short WEST = 4;

	/** North-South direction */
	short NORTH_SOUTH = 5;

	/** East-West direction */
	short EAST_WEST = 6;

	/** Inner Loop direction */
	short INNER_LOOP = 7;

	/** Outer Loop direction */
	short OUTER_LOOP = 8;

	/** Set the abbreviated name */
	void setAbbrev(String a);

	/** Get the abbreviated name */
	String getAbbrev();

	/** Set the roadway class */
	void setRClass(short c) throws TMSException;

	/** Get the roadway class */
	short getRClass();

	/** Set direction */
	void setDirection(short d) throws TMSException;

	/** Get direction */
	short getDirection();

	/** Set alternate direction */
	void setAltDir(short a) throws TMSException;

	/** Get alternate direction */
	short getAltDir();
}
