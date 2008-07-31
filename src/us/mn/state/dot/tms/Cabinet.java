/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
 * Cabinet
 *
 * @author Douglas Lau
 */
public interface Cabinet extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "cabinet";

	/** Set the cabinet style */
	void setStyle(CabinetStyle s);

	/** Get the cabinet style */
	CabinetStyle getStyle();

	/** Set the controller location */
	void setGeoLoc(GeoLoc l);

	/** Get the controller location */
	GeoLoc getGeoLoc();

	/** Set the milepoint */
	void setMile(Float m);

	/** Get the milepoint */
	Float getMile();
}
