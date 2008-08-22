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
 * Remote interface for the LaneControlSignalImpl
 *
 * @author    Douglas Lau
 * @author    Erik Engstrom
 */
public interface LaneControlSignal extends TrafficDevice {

	/** LCS is on and no modules are in error */
	public int STATUS_ON = 4;

	/** LCS is off and no modules are in error */
	public int STATUS_OFF = 5;

	/** LCS is reporting an error */
	public int STATUS_ERROR = 6;

	/** Set verification camera */
	public void setCamera( String id ) throws TMSException,
		RemoteException;

	/** Get verification camera */
	public String getCamera() throws RemoteException;

	/** Get the states of each module of this signal */
	public int[] getSignals() throws RemoteException;

	/** Set the states of each module of this signal */
	public void setSignals( int[] states, String user ) throws
			TMSException, RemoteException;

	/** Get the number of lanes */
	public int getLanes() throws RemoteException;

	/** Get the individual modules */
	public LCSModule[] getModules() throws RemoteException;
}
