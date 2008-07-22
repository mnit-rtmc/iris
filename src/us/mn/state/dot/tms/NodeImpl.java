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

import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * NodeImpl
 *
 * @author Douglas Lau
 */
public class NodeImpl extends TMSObjectImpl implements Node, ErrorCounter,
	Storable
{
	/** ObjectVault table name */
	static public final String tableName = "node";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Node ID regex pattern */
	static protected final Pattern ID_PATTERN =
		Pattern.compile("[0-9]{1,9}");

	/** Create a new node */
	public NodeImpl(NodeGroupImpl group, String i)
		throws TMSException, RemoteException
	{
		node_group = group;
		Matcher m = ID_PATTERN.matcher(i);
		if(!m.matches()) throw
			new ChangeVetoException("Invalid Node ID: " + i);
		id = i;
		geo_loc = "nod_" + id;
		GeoLocImpl loc = new GeoLocImpl(geo_loc);
		loc.doStore();
		MainServer.server.addObject(loc);
		notes = "";
	}

	/** Create a node from an ObjectVault field map */
	protected NodeImpl(FieldMap fields) throws RemoteException {
		node_group = (NodeGroupImpl)fields.get("node_group");
		id = (String)fields.get("id");
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		// FIXME: implement this for SONAR
		return null;
	}

	/** Initialize the transient fields */
	public void initTransients() throws ObjectVaultException,
		TMSException, RemoteException
	{
		super.initTransients();
		circuits.clear();
	}

	/** Add a circuit at system startup */
	public synchronized void addCircuit(CircuitImpl c) {
		circuits.add(c);
	}

	/** Get a string representation of the node */
	public String toString() {
		return id + ": " + GeoLocHelper.getDescription(lookupGeoLoc());
	}

	/** Node group for this node */
	protected final NodeGroupImpl node_group;

	/** Get the node group */
	public NodeGroup getGroup() { return node_group; }

	/** Node ID */
	protected final String id;

	/** Get the node ID */
	public String getId() { return id; }

	/** Node location */
	protected String geo_loc;

	/** Set the controller location */
	public synchronized void setGeoLoc(String l) throws TMSException {
		if(l == geo_loc)
			return;
		store.update(this, "geo_loc", l);
		geo_loc = l;
	}

	/** Get the node location */
	public String getGeoLoc() {
		return geo_loc;
	}

	/** Lookup the geo location */
	public GeoLocImpl lookupGeoLoc() {
		return lookupGeoLoc(geo_loc);
	}

	/** Array of circuits within this node */
	protected transient TreeSet<CircuitImpl> circuits =
		new TreeSet<CircuitImpl>(new Comparator<CircuitImpl>()
	{
		public int compare(CircuitImpl c1, CircuitImpl c2) {
			return c1.getId().compareTo(c2.getId());
		}
	});

	/** Insert a circuit into this node */
	public synchronized void insertCircuit(String c, CommunicationLine l)
		throws TMSException, RemoteException
	{
		CommunicationLineImpl line = lineList.findLine(l);
		if(line == null) throw
			new ChangeVetoException("Line not found");
		CircuitImpl circuit = new CircuitImpl(this, id + c, line);
		if(findCircuit(circuit.getId()) != null)
			throw new ChangeVetoException("Duplicate ID");
		try { vault.save(circuit, getUserName()); }
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		line.addCircuit(circuit);
		circuits.add(circuit);
	}

	/** Delete a circuit from this node */
	public synchronized void deleteCircuit(Circuit circuit)
		throws TMSException, RemoteException
	{
		CircuitImpl cir = findCircuit(circuit);
		if(cir == null) throw new
			ChangeVetoException("Circuit not found");
		if(!cir.isDeletable()) throw new
			ChangeVetoException("Circuit not deletable");
		CommunicationLineImpl line =
			(CommunicationLineImpl)cir.getLine();
		try { vault.delete(cir, getUserName()); }
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		line.removeCircuit(cir);
		circuits.remove(cir);
	}

	/** Find a circuit with the specified ID */
	public synchronized CircuitImpl findCircuit(String id) {
		for(CircuitImpl c: circuits) {
			if(c.getId().equals(id))
				return c;
		}
		return null;
	}

	/** Find the implementation of a specified circuit stub */
	protected CircuitImpl findCircuit(Circuit circuit) {
		for(CircuitImpl c: circuits) {
			if(c.equals(circuit))
				return c;
		}
		return null;
	}

	/** Get an array of all circuits in this node */
	public Circuit[] getCircuits() {
		return (Circuit [])circuits.toArray(new Circuit[0]);
	}

	/** Administrator notes for this node */
	protected String notes;

	/** Get the administrator notes */
	public String getNotes() { return notes; }

	/** Set the administrator notes */
	public synchronized void setNotes(String n) throws TMSException {
		if(n.equals(notes)) return;
		store.update(this, "notes", n);
		notes = n;
	}

	/** Get summed counters for all circuits in this node */
	public synchronized int[][] getCounters() {
		int[][] counters = new int[TYPES.length][PERIODS.length];
		for(CircuitImpl ci: circuits) {
			int[][] count = ci.getCounters();
			for(int c = 0; c < TYPES.length; c++)
				for(int p = 0; p < PERIODS.length; p++)
					counters[c][p] += count[c][p];
		}
		return counters;
	}

	/** Notify all observers for a status change */
	public synchronized void notifyStatus() {
		for(CircuitImpl circuit: circuits)
			circuit.notifyStatus();
		super.notifyStatus();
	}
}
