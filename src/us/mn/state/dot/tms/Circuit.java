/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2002  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms;

import java.rmi.RemoteException;

/**
 * Circuit
 *
 * @author Douglas Lau
 */
public interface Circuit extends TMSObject {

	/** Get the circuit ID */
	public String getId() throws RemoteException;

	/** Get the communication line for this circuit */
	public CommunicationLine getLine() throws RemoteException;

	/** Get all controllers for this circuit */
	public Controller[] getControllers() throws RemoteException;

	/** Add a controller at the specified drop address */
	public Controller addController(short drop) throws TMSException,
		RemoteException;

	/** Remove a controller from the specified drop address */
	public void removeController(short drop) throws TMSException,
		RemoteException;
}
