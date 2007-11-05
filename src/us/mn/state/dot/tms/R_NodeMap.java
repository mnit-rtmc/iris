/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
 * This is a map containing all r_node objects.
 *
 * @author Douglas Lau
 */
public interface R_NodeMap extends AbstractList {

	/** Add a new r_node to the map */
	R_Node add() throws TMSException, RemoteException;

	/** Remove an r_node from the map */
	void remove(R_Node r) throws TMSException, RemoteException;

	/** Get an r_node from its object ID */
	R_Node getElement(Integer oid) throws RemoteException;
}
