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

import java.rmi.RemoteException;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * NodeGroupList
 *
 * @author Douglas Lau
 */
class NodeGroupList extends IndexedListImpl {

	/** Create a new node group list */
	public NodeGroupList() throws RemoteException {
		super(false);
	}

	/** Append a node group to the list */
	public synchronized TMSObject append() throws TMSException,
		RemoteException
	{
		int index = list.size();
		NodeGroupImpl group = new NodeGroupImpl(index + 1);
		try {
			vault.save(group, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		list.add(group);
		notifyAdd(index, group.toString());
		return group;
	}

	/** Notify all observers for a status change */
	public synchronized void notifyStatus() {
		for(TMSObjectImpl object: list)
			object.notifyStatus();
		super.notifyStatus();
	}

	/** Find a circuit with the specified ID */
	public synchronized CircuitImpl findCircuit(String cid) {
		for(TMSObjectImpl object: list) {
			NodeGroupImpl ng = (NodeGroupImpl)object;
			CircuitImpl c = ng.findCircuit(cid);
			if(c != null)
				return c;
		}
		return null;
	}
}
