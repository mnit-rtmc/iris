/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The LCSModule is the remote interface of an individual module of a
 * LaneControlSignal.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @author Douglas Lau
 */
public interface LCSModule extends Remote {

	/** LCSModule state value */
	int DARK = 0;

	/** LCSModule state value */
	int GREEN = 1;

	/** LCSModule state value */
	int YELLOW = 2;

	/** LCSModule state value */
	int RED = 3;

	/** LCSModule state value */
	int ERROR = 4;

	/** Get the special function output for a module state */
	int getSFO(int state) throws RemoteException;

	/** Set the special function output for a module state */
	void setSFO(int state, int sfoValue) throws TMSException,
		RemoteException;

	/** Get the special function input for a module state */
	int getSFI(int state) throws RemoteException;

	/** Set the special function input for a module state */
	void setSFI(int state, int sfiValue) throws TMSException,
		RemoteException;
}
