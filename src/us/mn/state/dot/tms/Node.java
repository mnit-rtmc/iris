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
 * Node
 *
 * @author Douglas Lau
 */
public interface Node extends TMSObject {

	/** Get the node ID */
	public String getId() throws RemoteException;

	/** Get the node group */
	public NodeGroup getGroup() throws RemoteException;

	/** Get the node location */
	public String getGeoLoc() throws RemoteException;

	/** Set the node location */
	void setGeoLoc(String l) throws TMSException, RemoteException;

	/** Insert a circuit into this node */
	public void insertCircuit(String c, CommunicationLine l)
		throws TMSException, RemoteException;

	/** Delete a circuit from this node */
	public void deleteCircuit(Circuit c) throws TMSException,
		RemoteException;

	/** Get an array of all circuits in this node */
	public Circuit[] getCircuits() throws RemoteException;

	/** Get the administrator notes */
	public String getNotes() throws RemoteException;

	/** Set the administrator notes */
	public void setNotes(String n) throws TMSException, RemoteException;
}
