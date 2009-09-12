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

/**
 * A lane marking is a dynamically-controlled lane striping, such as
 * in-pavement LED lighting.
 *
 * @author Douglas Lau
 */
public interface LaneMarking extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "lane_marking";

	/** Get the device location */
	GeoLoc getGeoLoc();

	/** Set the deployed status */
	void setDeployed(boolean d);

	/** Get the deployed status */
	boolean getDeployed();
}
