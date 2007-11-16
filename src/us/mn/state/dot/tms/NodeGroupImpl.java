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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * NodeGroupImpl
 *
 * @author Douglas Lau
 */
class NodeGroupImpl extends TMSObjectImpl implements NodeGroup, ErrorCounter,
	Storable
{
	/** ObjectVault table name */
	static public final String tableName = "node_group";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Create a new node group */
	public NodeGroupImpl(int i) throws RemoteException {
		index = i;
		nodes = new Node[0];
	}

	/** Create a node group from an ObjectVault field map */
	protected NodeGroupImpl(FieldMap fields) throws RemoteException {
		index = fields.getInt("index");
	}

	/** Get a string representation */
	public String toString() {
		StringBuffer buf = new StringBuffer().append(index);
		while(buf.length() < 2)
			buf.insert(0, ' ');
		buf.append("> ").append(description);
		return buf.toString();
	}

	/** Node group index */
	protected final int index;

	/** Get the node group index */
	public int getIndex() {
		return index;
	}

	/** Node group description. (Sonet ring 2) */
	protected String description = "<New Node Group>";

	/** Set the description */
	public synchronized void setDescription(String d) throws TMSException {
		if(d.equals(description))
			return;
		validateText(d);
		store.update(this, "description", d);
		description = d;
	}

	/** Get the description */
	public String getDescription() {
		return description;
	}

	/** Array of nodes within this node group */
	protected transient Node[] nodes;

	/** Initialize the transient fields */
	public void initTransients() throws ObjectVaultException,
		TMSException, RemoteException
	{
		super.initTransients();
		ArrayList list = new ArrayList();
		Iterator it = vault.lookup(NodeImpl.class, "id");
		while(it.hasNext()) {
			NodeImpl node = (NodeImpl)vault.load(it.next());
			node.initTransients();
			if(node.getGroup() == this)
				list.add(node);
		}
		nodes = (Node [])list.toArray(new NodeImpl[0]);
	}

	/** Insert a node into this node group */
	public synchronized void insertNode(char n) throws TMSException,
		RemoteException
	{
		if(!Character.isLetterOrDigit(n))
			throw new ChangeVetoException("Invalid node ID");
		NodeImpl node = new NodeImpl(this, Integer.toString(index) + n);
		if(containsNodeId(node.getId()))
			throw new ChangeVetoException("Duplicate ID");
		try {
			vault.save(node, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		Node[] ns = new Node[nodes.length + 1];
		System.arraycopy(nodes, 0, ns, 1, nodes.length);
		ns[0] = node;
		Comparator comp = new Comparator() {
			public int compare(Object n1, Object n2) {
				return ((NodeImpl)n1).getId().compareTo(
					((NodeImpl)n2).getId());
			}
		};
		Arrays.sort(ns, comp);
		nodes = ns;
	}

	/** Delete a node from this node group */
	public synchronized void deleteNode(Node node) throws TMSException,
		RemoteException
	{
		NodeImpl nod = findNode(node);
		if(nod == null)
			throw new ChangeVetoException("Node not found");
		if(!nod.isDeletable())
			throw new ChangeVetoException("Node not deletable");
		try {
			vault.delete(nod, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		Node[] ns = new Node[nodes.length - 1];
		for(int i = 0, j = 0; i < nodes.length; i++) {
			Node n = nodes[i];
			if(n.equals(nod))
				((NodeImpl)n).notifyDelete();
			else
				ns[j++] = n;
		}
		nodes = ns;
	}

	/** Check if the group contains a specified node ID */
	protected boolean containsNodeId(String id) {
		for(int i = 0; i < nodes.length; i++)
			if(((NodeImpl)nodes[i]).getId().equals(id))
				return true;
		return false;
	}

	/** Find the implementation of a specified node stub */
	protected NodeImpl findNode(Node node) {
		for(int i = 0; i < nodes.length; i++) {
			if(nodes[i].equals(node))
				return (NodeImpl)nodes[i];
		}
		return null;
	}

	/** Get an array of all nodes in this node group */
	public Node[] getNodes() {
		Node[] result = new Node[nodes.length];
		for(int i = 0; i < nodes.length; i++)
			result[i] = nodes[i];
		return result;
	}

	/** Get summed counters for all nodes in this node group */
	public synchronized int[][] getCounters() {
		int[][] counters = new int[TYPES.length][PERIODS.length];
		for(int n = 0; n < nodes.length; n++) {
			int[][] count = ((NodeImpl)nodes[n]).getCounters();
			for(int c = 0; c < TYPES.length; c++)
				for(int p = 0; p < PERIODS.length; p++)
					counters[c][p] += count[c][p];
		}
		return counters;
	}

	/** Notify all observers for a status change */
	public synchronized void notifyStatus() {
		for(int i = 0; i < nodes.length; i++) {
			NodeImpl node = (NodeImpl)nodes[i];
			node.notifyStatus();
		}
		super.notifyStatus();
	}

	/** Find a circuit with the specified ID */
	public synchronized CircuitImpl findCircuit(String cid) {
		for(int i = 0; i < nodes.length; i++) {
			NodeImpl node = (NodeImpl)nodes[i];
			CircuitImpl c = node.findCircuit(cid);
			if(c != null)
				return c;
		}
		return null;
	}
}
