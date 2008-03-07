/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * This is a map containing all r_node objects.
 *
 * @author Douglas Lau
 */
class R_NodeMapImpl extends AbstractListImpl implements R_NodeMap {

	/** R_Node list XML file */
	static protected final String R_NODE_XML = "r_nodes.xml";

	/** TreeMap to hold all the r_nodes */
	protected final TreeMap<Integer, TMSObjectImpl> map;

	/** Map to hold all corridors */
	protected final Map<String, Corridor> corridors =
		new TreeMap<String, Corridor>();

	/** Create a new r_node map */
	public R_NodeMapImpl() throws RemoteException {
		super(false);
		map = new TreeMap<Integer, TMSObjectImpl>();
	}

	/** Get an iterator of the r_nodes */
	Iterator<TMSObjectImpl> iterator() {
		return map.values().iterator();
	}

	/** Load the contents of the list from the ObjectVault */
	void load(Class c, String keyField) throws ObjectVaultException,
		TMSException, RemoteException
	{
		map.clear();
		Iterator it = vault.lookup(c, keyField);
		while(it.hasNext()) {
			R_NodeImpl r_node = (R_NodeImpl)vault.load(it.next());
			r_node.initTransients();
			map.put(r_node.getOID(), r_node);
		}
	}

	/** Put an r_node into the map */
	protected synchronized void put(R_NodeImpl r_node) {
		Integer oid = r_node.getOID();
		map.put(oid, r_node);
		Iterator<Integer> it = map.keySet().iterator();
		for(int i = 0; it.hasNext(); i++) {
			Integer search = it.next();
			if(oid.equals(search)) {
				notifyAdd(i, oid);
				return;
			}
		}
	}

	/** Add a new r_node to the map */
	public R_Node add() throws TMSException, RemoteException {
		R_NodeImpl r_node = new R_NodeImpl();
		try {
			vault.save(r_node, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		put(r_node);
		return r_node;
	}

	/** Remove an r_node from the map */
	public synchronized void remove(R_Node r) throws TMSException,
		RemoteException
	{
		Integer oid = r.getOID();
		R_NodeImpl r_node = (R_NodeImpl)map.get(oid);
		if(r_node == null)
			throw new ChangeVetoException("Invalid OID: " + oid);
		if(!r_node.isDeletable())
			throw new ChangeVetoException("Cannot delete r_node");
		try {
			vault.delete(r_node, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		Iterator<Integer> it = map.keySet().iterator();
		for(int i = 0; it.hasNext(); i++) {
			Integer search = it.next();
			if(oid.equals(search)) {
				it.remove();
				notifyRemove(i);
				r_node.notifyDelete();
				return;
			}
		}
	}

	/** Get a single r_node from its key */
	public synchronized R_Node getElement(Integer oid) {
		if(oid == null)
			return null;
		return (R_Node)map.get(oid);
	}

	/** Subscribe a listener to this list */
	public synchronized Object[] subscribe(RemoteList listener) {
		super.subscribe(listener);
		if(map.size() < 1)
			return null;
		Object[] list = new Object[map.size()];
		Iterator<TMSObjectImpl> it = iterator();
		for(int i = 0; it.hasNext(); i++)
			list[i] = it.next().getOID();
		return list;
	}

	/** Get the r_node with the associated detector */
	public synchronized R_NodeImpl getR_Node(DetectorImpl det) {
		if(det == null)
			return null;
		for(TMSObjectImpl r_node: map.values()) {
			if(((R_NodeImpl)r_node).hasDetector(det))
				return (R_NodeImpl)r_node;
		}
		return null;
	}

	/** Find the nearest r_node in a list */
	static protected R_NodeImpl findNearest(R_NodeImpl r_node,
		List<R_NodeImpl> others)
	{
		R_NodeImpl nearest = null;
		double distance = 0;
		for(R_NodeImpl other: others) {
			double m = r_node.metersTo(other);
			if(nearest == null || m < distance) {
				nearest = other;
				distance = m;
			}
		}
		return nearest;
	}

	/** Link an exit node with a corresponding entrance node */
	protected void linkExitToEntrance(R_NodeImpl r_node) {
		LinkedList<R_NodeImpl> links = new LinkedList<R_NodeImpl>();
		for(TMSObjectImpl o: map.values()) {
			R_NodeImpl other = (R_NodeImpl)o;
			if(r_node.isExitLink(other))
				links.add(other);
		}
		R_NodeImpl link = findNearest(r_node, links);
		if(link != null)
			r_node.addDownstream(link);
	}

	/** Link an access node with all corresponding entrance nodes */
	protected void linkAccessToEntrance(R_NodeImpl r_node) {
		for(TMSObjectImpl o: map.values()) {
			R_NodeImpl other = (R_NodeImpl)o;
			if(r_node.isAccessLink(other))
				r_node.addDownstream(other);
		}
	}

	/** Find downstream links (not in corridor) for the given node */
	protected void findDownstreamLinks(R_NodeImpl r_node) {
		r_node.clearDownstream();
		if(r_node.isExit())
			linkExitToEntrance(r_node);
		else if(r_node.isAccess())
			linkAccessToEntrance(r_node);
		// FIXME: link intersections together
	}

	/** Create all corridors from the existing r_nodes */
	protected void createCorridors() {
		corridors.clear();
		for(TMSObjectImpl n: map.values()) {
			R_NodeImpl r_node = (R_NodeImpl)n;
			findDownstreamLinks(r_node);
			LocationImpl loc = (LocationImpl)r_node.getLocation();
			String cid = loc.getCorridor();
			if(cid != null) {
				Corridor c = corridors.get(cid);
				if(c == null) {
					c = new Corridor(false, loc);
					corridors.put(cid, c);
				}
				c.addNode(r_node);
			}
		}
	}

	/** Write the r_node configuration in XML format */
	public void writeXml() throws IOException {
		XmlWriter w = new XmlWriter(R_NODE_XML, false) {
			public void print(PrintWriter out) {
				printXmlBody(out);
			}
		};
		w.write();
	}

	/** Print the body of the r_node configuration XML file */
	protected synchronized void printXmlBody(PrintWriter out) {
		createCorridors();
		for(Corridor c: corridors.values())
			c.printXml(out);
	}

	/** Lookup the named corridor */
	public synchronized Corridor getCorridor(String c) {
		if(c != null)
			return corridors.get(c);
		else
			return null;
	}

	/** Lookup the corridor for an O/D pair */
	protected Corridor getCorridor(ODPair od) {
		return getCorridor(od.getCorridor());
	}
}
