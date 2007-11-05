/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.rmi.RemoteException;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeMap;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.proxy.ProxyHandlerImpl;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;

/**
 * R_Node handler provides proxies for roadway nodes.
 *
 * @author Douglas Lau
 */
public class R_NodeHandler extends ProxyHandlerImpl {

	/** Remote roadway node map */
	protected final R_NodeMap r_node_map;

	/** Set of all defined corridors */
	protected final TreeSet<String> corridors = new TreeSet<String>();

	/** List model of all corridors */
	protected final DefaultListModel model = new DefaultListModel();

	/** Get the corridor list model */
	public ListModel getCorridorModel() {
		return model;
	}

	/** Get the proxy type */
	public String getProxyType() {
		return R_NodeProxy.PROXY_TYPE;
	}

	/** Create a new roadway node handler */
	protected R_NodeHandler(TmsConnection tc, R_NodeMap n_map)
		throws RemoteException
	{
		super(tc, n_map, new R_NodeMapTheme());
		r_node_map = n_map;
		initialize();
	}

	/** Add a corridor to the corridor model */
	protected void addCorridor(String corridor) {
		if(corridors.add(corridor)) {
			Iterator<String> it = corridors.iterator();
			for(int i = 0; it.hasNext(); i++) {
				String c = it.next();
				if(corridor.equals(c)) {
					model.add(i, c);
					return;
				}
			}
		}
	}

	/** Create a set of roadway nodes for the specified corridor */
	public Set<R_NodeProxy> createSet(String corridor)
		throws RemoteException
	{
		HashSet<R_NodeProxy> nodes = new HashSet<R_NodeProxy>();
		synchronized(proxies) {
			for(TmsMapProxy proxy: proxies.values()) {
				R_NodeProxy node = (R_NodeProxy)proxy;
				if(node.getCorridor().equals(corridor))
					nodes.add(node);
			}
		}
		return nodes;
	}

	/** Load an R_NodeProxy by oid */
	protected TmsMapProxy loadProxy(Object oid) throws RemoteException {
		R_Node r_node = r_node_map.getElement((Integer)oid);
		R_NodeProxy proxy = new R_NodeProxy(r_node);
		addCorridor(proxy.getCorridor());
		return proxy;
	}

	/** File the proxy in the approriate list models */
	protected void fileProxy(final TmsMapProxy proxy) {
		// Hmmm... nothing to do
	}

	/** Add a new roadway node */
	protected R_Node addNode() throws TMSException, RemoteException {
		return r_node_map.add();
	}

	/** Remove a roadway node */
	protected void removeNode(R_NodeProxy proxy) throws TMSException,
		RemoteException
	{
		r_node_map.remove(proxy.r_node);
	}

	/** Create the roadway node layer */
	static public R_NodeLayer createLayer(TmsConnection tc)
		throws RemoteException
	{
		R_NodeMap n_map =
			(R_NodeMap)tc.getProxy().getR_Nodes().getList();
		return new R_NodeLayer(new R_NodeHandler(tc, n_map));
	}
}
