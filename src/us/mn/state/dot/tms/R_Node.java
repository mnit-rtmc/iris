/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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

import java.rmi.RemoteException;

/**
 * An R_Node represents one node on the roadway network.
 *
 * @author Douglas Lau
 */
public interface R_Node extends TMSObject {

	/** Node type descriptions */
	String[] TYPES = {
		"Station", "Entrance", "Exit", "Intersection", "Access",
		"Interchange"
	};

	/** Station r_node type */
	int TYPE_STATION = 0;

	/** Entrance r_node type */
	int TYPE_ENTRANCE = 1;

	/** Exit r_node type */
	int TYPE_EXIT = 2;

	/** Intersection r_node type */
	int TYPE_INTERSECTION = 3;

	/** Access r_node type */
	int TYPE_ACCESS = 4;

	/** Interchange r_node type */
	int TYPE_INTERCHANGE = 5;

	/** Transition descriptions */
	String[] TRANSITIONS = {
		"None", "Loop", "Leg", "Slipramp", "CD", "HOV", "Common",
		"Flyover"
	};

	/** None transition */
	int TRANSITION_NONE = 0;

	/** Loop transition */
	int TRANSITION_LOOP = 1;

	/** Leg transition */
	int TRANSITION_LEG = 2;

	/** Slipramp transition */
	int TRANSITION_SLIPRAMP = 3;

	/** CD road transition */
	int TRANSITION_CD = 4;

	/** HOV transition */
	int TRANSITION_HOV = 5;

	/** Common section transition */
	int TRANSITION_COMMON = 6;

	/** Flyover transition */
	int TRANSITION_FLYOVER = 7;

	/** Get the location */
	String getGeoLoc() throws RemoteException;

	/** Set the location */
	void setGeoLoc(String l) throws TMSException, RemoteException;

	/** Get the node type */
	int getNodeType() throws RemoteException;

	/** Set the node type */
	void setNodeType(int t) throws TMSException, RemoteException;

	/** Is this node pickable? */
	boolean isPickable() throws RemoteException;

	/** Set the pickable flag */
	void setPickable(boolean p) throws TMSException, RemoteException;

	/** Get the transition type */
	int getTransition() throws RemoteException;

	/** Set the transition type */
	void setTransition(int t) throws TMSException, RemoteException;

	/** Get the number of lanes */
	int getLanes() throws RemoteException;

	/** Set the number of lanes */
	void setLanes(int l) throws TMSException, RemoteException;

	/** Get the attach side (true = left, false = right) */
	boolean getAttachSide() throws RemoteException;

	/** Set the attach side */
	void setAttachSide(boolean s) throws TMSException, RemoteException;

	/** Get the lane shift */
	int getShift() throws RemoteException;

	/** Set the lane shift */
	void setShift(int s) throws TMSException, RemoteException;

	/** Get the station ID */
	String getStationID() throws RemoteException;

	/** Set the station ID */
	void setStationID(String s) throws TMSException, RemoteException;

	/** Get the speed limit */
	int getSpeedLimit() throws RemoteException;

	/** Set the speed limit */
	void setSpeedLimit(int l) throws TMSException, RemoteException;

	/** Get the administrator notes */
	String getNotes() throws RemoteException;

	/** Set the administrator notes */
	void setNotes(String n) throws TMSException, RemoteException;

	/** Get all detectors with a matching location */
	Detector[] getMatchingDetectors() throws RemoteException;

	/** Get an array of all node detectors */
	Detector[] getDetectors() throws RemoteException;

	/** Set the array of detectors */
	void setDetectors(Detector[] dets) throws TMSException, RemoteException;
}
