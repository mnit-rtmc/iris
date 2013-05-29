/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
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
 * An R_Node represents one node on the roadway network.
 *
 * @author Douglas Lau
 */
public interface R_Node extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "r_node";

	/** Minimum lane shift */
	int MIN_SHIFT = 1;

	/** Maximum lane shift */
	int MAX_SHIFT = 9;

	/** Maximum number of lanes */
	int MAX_LANES = MAX_SHIFT - MIN_SHIFT;

	/** Middle shift value */
	int MID_SHIFT = MIN_SHIFT + MAX_LANES / 2;

	/** Set the location.  This is needed for creating a new phantom r_node
	 * with SONAR.  It is an error to call this method after the r_node has
	 * been created. */
	void setGeoLoc(GeoLoc loc);

	/** Get the location */
	GeoLoc getGeoLoc();

	/** Set the node type */
	void setNodeType(int t);

	/** Get the node type */
	int getNodeType();

	/** Set the pickable flag */
	void setPickable(boolean p);

	/** Is this node pickable? */
	boolean getPickable();

	/** Set the above flag */
	void setAbove(boolean t);

	/** Is the node above others? */
	boolean getAbove();

	/** Set the transition type */
	void setTransition(int t);

	/** Get the transition type */
	int getTransition();

	/** Set the number of lanes */
	void setLanes(int l);

	/** Get the number of lanes */
	int getLanes();

	/** Set the attach side */
	void setAttachSide(boolean s);

	/** Get the attach side (true = left, false = right) */
	boolean getAttachSide();

	/** Set the lane shift */
	void setShift(int s);

	/** Get the lane shift */
	int getShift();

	/** Activate / deactivate the r_node */
	void setActive(boolean a);

	/** Get the active state */
	boolean getActive();

	/** Set the abandoned state */
	void setAbandoned(boolean a);

	/** Get the abandoned state */
	boolean getAbandoned();

	/** Set the station ID */
	void setStationID(String s);

	/** Get the station ID */
	String getStationID();

	/** Set the speed limit */
	void setSpeedLimit(int l);

	/** Get the speed limit */
	int getSpeedLimit();

	/** Set the administrator notes */
	void setNotes(String n);

	/** Get the administrator notes */
	String getNotes();
}
