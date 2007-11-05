/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
	public Location getLocation() throws RemoteException;

	/** Get the administrator notes */
	public String getNotes() throws RemoteException;

	/** Set the administrator notes */
	public void setNotes(String n) throws TMSException, RemoteException;

	/** Set the milepoint */
	public void setMile(float m) throws TMSException, RemoteException;

	/** Get the milepoint */
	public float getMile() throws RemoteException;

	/** Set traffic device */
	public void setDevice(String id) throws TMSException, RemoteException;

	/** Get traffic device */
	public TrafficDevice getDevice() throws RemoteException;

	/** Set a data detector */
	public void setDetector(int input, int det) throws TMSException,
		RemoteException;

	/** Get a data detector */
	public Detector getDetector(int input) throws RemoteException;

	/** Add an alarm to the controller */
	public Alarm addAlarm(int pin) throws TMSException, RemoteException;

	/** Remove an alarm from the controller */
	public void removeAlarm(int pin) throws TMSException, RemoteException;

	/** Get an alarm from the controller */
	public Alarm getAlarm(int pin) throws RemoteException;

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
