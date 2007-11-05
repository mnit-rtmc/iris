/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2005  Minnesota Department of Transportation
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

import java.util.LinkedList;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.FieldMap;

/**
 * CircuitImpl
 *
 * @author Douglas Lau
 */
class CircuitImpl extends TMSObjectImpl implements Circuit, ErrorCounter {

	/** ObjectVault table name */
	static public final String tableName = "circuit";

	/** Circuit ID regex pattern */
	static protected final Pattern ID_PATTERN =
		Pattern.compile("[0-9]{1,9}");

	/** Create a new circuit */
	public CircuitImpl(NodeImpl n, String i, CommunicationLineImpl l)
		throws ChangeVetoException, RemoteException
	{
		node = n;
		Matcher m = ID_PATTERN.matcher(i);
		if(!m.matches()) throw
			new ChangeVetoException("Invalid Circuit ID: " + i);
		id = i;
		line = l;
		controllers = new LinkedList();
	}

	/** Create a circuit from an ObjectVault field map */
	protected CircuitImpl(FieldMap fields) throws RemoteException {
		node = (NodeImpl)fields.get("node");
		id = (String)fields.get("id");
		line = (CommunicationLineImpl)fields.get("line");
		controllers = new LinkedList();
	}

	/** Initialize the transient fields */
	public void initTransients() {
		node.addCircuit(this);
		line.addCircuit(this);
		controllers.clear();
	}

	/** Node for this circuit */
	protected NodeImpl node;

	/** Get the node */
	public Node getNode() { return node; }

	/** Circuit ID */
	protected final String id;

	/** Get the circuit ID */
	public final String getId() { return id; }

	/** Communication line associated with this circuit */
	protected final CommunicationLineImpl line;

	/** Get the communication line for this circuit */
	public CommunicationLine getLine() { return line; }

	/** Linked list of controllers within this circuit */
	protected transient LinkedList controllers;

	/** Get all controllers for this circuit */
	public synchronized Controller[] getControllers() {
		return (Controller [])controllers.toArray(new Controller[0]);
	}

	/** Add a controller at the specified drop address */
	public synchronized Controller addController(short drop)
		throws TMSException, RemoteException
	{
		Controller controller = line.addController(this, drop);
		controllers.add(controller);
		return controller;
	}

	/** Remove a controller from the specified drop address */
	public synchronized void removeController(short drop)
		throws TMSException
	{
		ControllerImpl controller = line.removeController(drop);
		if(!controllers.remove(controller)) throw new
			TMSException("Circuit controller mismatch");
	}

	/** Put an existing controller into this circuit */
	public synchronized void putController(ControllerImpl c) {
		line.putController(c);
		controllers.add(c);
	}

	/** Pull a controller from this circuit (move to another) */
	public synchronized void pullController(ControllerImpl c) {
		line.pullController(c);
		controllers.remove(c);
	}

	/** Get summed counters for all controllers on this circuit */
	public synchronized int[][] getCounters() {
		int[][] counters = new int[TYPES.length][PERIODS.length];
		Iterator it = controllers.iterator();
		while(it.hasNext()) {
			ControllerImpl controller = (ControllerImpl)it.next();
			if(!controller.isActive()) continue;
			int[][] count = controller.getCounters();
			for(int c = 0; c < TYPES.length; c++)
				for(int p = 0; p < PERIODS.length; p++)
					counters[c][p] += count[c][p];
		}
		return counters;
	}
}
