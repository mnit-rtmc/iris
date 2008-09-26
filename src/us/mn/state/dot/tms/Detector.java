/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
 * Detector for traffic data sampling
 *
 * @author Douglas Lau
 */
public interface Detector extends Device {

	/** Get the detector index */
	int getIndex() throws RemoteException;

	/** Set the lane type */
	void setLaneType(short t) throws TMSException, RemoteException;

	/** Get the lane type */
	short getLaneType() throws RemoteException;

	/** Is this a station detector? (mainline, non-HOV) */
	boolean isStation() throws RemoteException;

	/** Set the lane number */
	void setLaneNumber(short laneNumber) throws TMSException,
		RemoteException;

	/** Get the lane number */
	short getLaneNumber() throws RemoteException;

	/** Set the abandoned status */
	void setAbandoned(boolean abandoned) throws TMSException,
		RemoteException;

	/** Get the abandoned status */
	boolean isAbandoned() throws RemoteException;

	/** Set the Force Fail status */
	void setForceFail(boolean forceFail) throws TMSException,
		RemoteException;

	/** Get the Force Fail status */
	boolean getForceFail() throws RemoteException;

	/** Set the average field length */
	void setFieldLength(float field) throws TMSException, RemoteException;

	/** Get the average field length */
	float getFieldLength() throws RemoteException;

	/** Get the String representation of this detector */
	String getLabel() throws RemoteException;

	/** Get the detector label */
	String getLabel(boolean statName) throws RemoteException;

	/** Set the fake detector */
	void setFakeDetector(String f) throws TMSException, RemoteException;

	/** Get the fake detector */
	String getFakeDetector() throws RemoteException;

	/** Get the station which contains this detector */
	Station getStation() throws RemoteException;
}
