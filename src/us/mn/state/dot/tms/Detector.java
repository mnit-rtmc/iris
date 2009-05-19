/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
 * Detector for traffic data sampling
 *
 * @author Douglas Lau
 */
public interface Detector extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "detector";

	/** Set the r_node (roadway network node) */
	void setR_Node(R_Node n);

	/** Get the r_node (roadway network node) */
	R_Node getR_Node();

	/** Set the lane type */
	void setLaneType(short t);

	/** Get the lane type */
	short getLaneType();

	/** Set the lane number */
	void setLaneNumber(short n);

	/** Get the lane number */
	short getLaneNumber();

	/** Set the abandoned status */
	void setAbandoned(boolean a);

	/** Get the abandoned status */
	boolean getAbandoned();

	/** Set the Force Fail status */
	void setForceFail(boolean f);

	/** Get the Force Fail status */
	boolean getForceFail();

	/** Set the average field length (feet) */
	void setFieldLength(float f);

	/** Get the average field length (feet) */
	float getFieldLength();

	/** Set the fake expression */
	void setFake(String f);

	/** Get the fake expression */
	String getFake();
}
