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
 * A map extent is a predefined extent (view) for map toolbar buttons.
 *
 * @author Douglas Lau
 */
public interface MapExtent extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "map_extent";

	/** Set the UTM Easting */
	void setEasting(int x);

	/** Get the UTM Easting */
	int getEasting();

	/** Set the UTM Easting span */
	void setEastSpan(int x);

	/** Get the UTM Easting span */
	int getEastSpan();

	/** Set the UTM Northing */
	void setNorthing(int y);

	/** Get the UTM Northing */
	int getNorthing();

	/** Set the UTM Northing span */
	void setNorthSpan(int y);

	/** Get the UTM Northing span */
	int getNorthSpan();
}
