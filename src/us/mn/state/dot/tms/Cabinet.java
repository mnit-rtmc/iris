/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
 * Copyright (C) 2014  AHMCT, University of California
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
 * The cabinet interface represents a roadside enclosure containing one or more
 * device controllers.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public interface Cabinet extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "cabinet";

	/** Set the cabinet style */
	void setStyle(CabinetStyle s);

	/** Get the cabinet style */
	CabinetStyle getStyle();

	/** Get the cabinet location */
	GeoLoc getGeoLoc();

}
