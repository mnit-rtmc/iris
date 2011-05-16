/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

	/** Set the longitude */
	void setLon(float ln);

	/** Get the longitude */
	float getLon();

	/** Set the latitude */
	void setLat(float lt);

	/** Get the latitude */
	float getLat();

	/** Set the zoom level */
	void setZoom(int z);

	/** Get the zoom level */
	int getZoom();
}
