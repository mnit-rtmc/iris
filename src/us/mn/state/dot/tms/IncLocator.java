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
 * An incident locator is part of a message to deploy on a DMS, matching
 * incident attributes.
 *
 * @author Douglas Lau
 */
public interface IncLocator extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "inc_locator";

	/** Set the range */
	void setRange(int r);

	/** Get the range */
	int getRange();

	/** Set the branched flag */
	void setBranched(boolean b);

	/** Get the branched flag */
	boolean getBranched();

	/** Set the picked flag */
	void setPicked(boolean p);

	/** Get the picked flag */
	boolean getPicked();

	/** Set the MULTI string */
	void setMulti(String m);

	/** Get the MULTI string */
	String getMulti();
}
