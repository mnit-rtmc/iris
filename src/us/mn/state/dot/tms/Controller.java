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

import java.util.Date;
import java.rmi.RemoteException;

/**
 * Controller
 *
 * @author Douglas Lau
 */
public interface Controller extends TMSObject {

	/** All I/O pins */
	int ALL_PINS = 104;

	/** I/O pin for first traffic device */
	int DEVICE_PIN = 1;

	/** Get controller label (Line x drop y) */
	public String getLabel() throws RemoteException;

	/** Set the circuit for this controller */
	public void setCircuit(String circuitId) throws TMSException,
		RemoteException;

	/** Get controller circuit */
	public Circuit getCircuit() throws RemoteException;

	/** Get the communication line */
	public CommunicationLine getLine() throws RemoteException;

	/** Get the drop address */
	public short getDrop() throws RemoteException;

	/** Set the drop address */
	public void setDrop(short d) throws TMSException, RemoteException;

	/** Set the active status */
	public void setActive(boolean a) throws TMSException, RemoteException;

	/** Get the active status */
	public boolean isActive() throws RemoteException;

	/** Get the controller location */
	String getGeoLoc() throws RemoteException;

	/** Set the controller location */
	void setGeoLoc(String l) throws TMSException, RemoteException;

	/** Get the administrator notes */
	public String getNotes() throws RemoteException;

	/** Set the administrator notes */
	public void setNotes(String n) throws TMSException, RemoteException;

	/** Set the milepoint */
	public void setMile(float m) throws TMSException, RemoteException;

	/** Get the milepoint */
	public float getMile() throws RemoteException;

	/** Get all the Input/Output devices */
	ControllerIO[] getIO() throws RemoteException;

	/** Get the failure status */
	public boolean isFailed() throws RemoteException;

	/** Get the time stamp of the most recent comm failure */
	public Date getFailTime() throws RemoteException;

	/** Perform a controller download */
	public void download(boolean reset) throws RemoteException;

	/** Get the controller communication status */
	public String getStatus() throws RemoteException;

	/** Get the controller setup configuration state */
	public String getSetup() throws RemoteException;

	/** Get the controller firmware version */
	public String getVersion() throws RemoteException;

	/** Test the communications to this controller */
	public void testCommunications(boolean on_off) throws RemoteException;
}
