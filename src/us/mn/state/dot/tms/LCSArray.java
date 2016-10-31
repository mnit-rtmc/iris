/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.User;

/**
 * A Lane-Use Control Signal Array is a series of LCS devices across all lanes
 * of a roadway corridor.
 *
 * @author Douglas Lau
 */
public interface LCSArray extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "lcs_array";

	/** Set the lane shift of left lane */
	void setShift(int s);

	/** Get the lane shift of left lane */
	int getShift();

	/** Set the lock status code */
	void setLcsLock(Integer c);

	/** Get the lock status code */
	Integer getLcsLock();

	/** Set the next indications owner */
	void setOwnerNext(User o);

	/** Set the next lane-use indications */
	void setIndicationsNext(Integer[] ind);

	/** Get the owner of the current indications.
	 * @return User who deployed the current indications. */
	User getOwnerCurrent();

	/** Get the current lane-use indications */
	Integer[] getIndicationsCurrent();

	/** Get the operation description */
	String getOperation();
}
