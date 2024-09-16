/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2024  Minnesota Department of Transportation
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
 * A toll zone is a roadway segment which is tolled by usage.
 *
 * @author Douglas Lau
 */
public interface TollZone extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "toll_zone";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** Set the starting station ID */
	void setStartID(String sid);

	/** Get the starting station ID */
	String getStartID();

	/** Set the ending station ID */
	void setEndID(String sid);

	/** Get the ending station ID */
	String getEndID();

	/** Set the tollway ID */
	void setTollway(String tw);

	/** Get the tollway ID */
	String getTollway();

	/** Set the density alpha coefficient */
	void setAlpha(Float a);

	/** Get the density alpha coefficient */
	Float getAlpha();

	/** Set the density beta coefficient */
	void setBeta(Float b);

	/** Get the density beta coefficient */
	Float getBeta();

	/** Set the max price (dollars) */
	void setMaxPrice(Float p);

	/** Get the max price (dollars) */
	Float getMaxPrice();
}
