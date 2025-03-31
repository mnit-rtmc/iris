/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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
 * Event table configuration.
 *
 * @author Douglas Lau
 */
public interface EventConfig extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "event_config";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = SystemAttribute.SONAR_TYPE;

	/** Set enable store flag */
	void setEnableStore(boolean es);

	/** Get enable store flag */
	boolean getEnableStore();

	/** Set enable purge flag */
	void setEnablePurge(boolean ep);

	/** Get enable purge flag */
	boolean getEnablePurge();

	/** Set the number of days to keep events before purging */
	void setPurgeDays(int pd);

	/** Get the number of days to keep events before purging */
	int getPurgeDays();
}
