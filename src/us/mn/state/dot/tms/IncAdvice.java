/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2019  Minnesota Department of Transportation
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
 * An incident advice is part of a message to deploy on a DMS, matching
 * incident attributes.
 *
 * @author Douglas Lau
 */
public interface IncAdvice extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "inc_advice";

	/** Set the impact */
	void setImpact(int imp);

	/** Get the impact */
	int getImpact();

	/** Set the lane type ordinal */
	void setLaneType(short lt);

	/** Get the lane type ordinal */
	short getLaneType();

	/** Set the range */
	void setRange(int r);

	/** Get the range */
	int getRange();

	/** Set count of open lanes */
	void setOpenLanes(Integer op);

	/** Get count of open lanes */
	Integer getOpenLanes();

	/** Set count of impacted lanes */
	void setImpactedLanes(Integer ln);

	/** Get count of impacted lanes */
	Integer getImpactedLanes();

	/** Set the MULTI string */
	void setMulti(String m);

	/** Get the MULTI string */
	String getMulti();
}
