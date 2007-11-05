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
package us.mn.state.dot.tms.client.proxy;

import java.rmi.RemoteException;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import us.mn.state.dot.map.Theme;
import us.mn.state.dot.tms.AbstractList;
import us.mn.state.dot.tms.utils.RemoteListModel;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.TmsSelectionModel;
import us.mn.state.dot.tms.client.security.IrisUser;

/**
 * A proxy handler is a container for TMS object proxy objects.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
abstract public class ProxyHandlerImpl extends RemoteListModel
	implements ProxyHandler
{
	/** List of refresh listeners.
	 * NOTE: this object may only be accessed by the Swing thread */
	protected final EventListenerList listeners = new EventListenerList();

	/** Map of all TMS object proxies of this class */
	protected final Map<Object, TmsMapProxy> proxies =
		new HashMap<Object, TmsMapProxy>();

	/** Get the map of all handler proxies. Warning: must synchronize
	 * access if not on RWORKER thread. */
	public Map<Object, TmsMapProxy> getProxies() {
		return proxies;
	}

	/** Selection model */
	protected final TmsSelectionModel selectionModel =
		new TmsSelectionModel();

	/** Connection to TMS server */
	protected final TmsConnection connection;

	/** Get the connection to the TMS server */
	public TmsConnection getConnection() {
		return connection;
	}

	/** Theme for proxy based on status */
	protected final Theme theme;

	/** Get the theme */
	public Theme getTheme() {
		return theme;
	}

	/** Create a new proxy handler */
	protected ProxyHandlerImpl(TmsConnection c, AbstractList l, Theme t)
		throws RemoteException
	{
		super(l, false);
		connection = c;
		theme = t;
	}

	/** Refresh the status of all proxies in the handler */
	protected void doStatus() throws RemoteException {
		synchronized(proxies) {
			for(TmsMapProxy proxy: proxies.values())
				proxy.updateStatusInfo();
		}
		fireDataChanged();
	}

	/** Add a new refresh listener */
	public void addRefreshListener(final RefreshListener l) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				listeners.add(RefreshListener.class, l);
			}
		});
	}

	/** Remove a refresh listener */
	public void removeRefreshListener(final RefreshListener l) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				listeners.remove(RefreshListener.class, l);
			}
		});
	}

	/** Notify all RefreshListeners that the data has changed */
	protected void _fireDataChanged() {
		EventListener[] el =
			listeners.getListeners(RefreshListener.class);
		for(int i = 0; i < el.length; i++) {
			RefreshListener l = (RefreshListener)el[i];
			l.dataChanged();
		}
	}

	/** Notify all RefreshListeners that the data has changed */
	protected void fireDataChanged() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_fireDataChanged();
			}
		});
	}

	/** Load a TMS map proxy by id */
	abstract protected TmsMapProxy loadProxy(Object id)
		throws RemoteException;

	/** File the proxy in the approriate list models */
	abstract protected void fileProxy(final TmsMapProxy proxy);

	/** Get the proxy by its id */
	public TmsMapProxy getProxy(Object id) {
		synchronized(proxies) {
			return proxies.get(id);
		}
	}

	/** Force a proxy into proxy cache */
	protected void cacheProxy(Object id) throws RemoteException {
		synchronized(proxies) {
			if(proxies.containsKey(id))
				return;
		}
		TmsMapProxy proxy = loadProxy(id);
		synchronized(proxies) {
			proxies.put(id, proxy);
		}
		fileProxy(proxy);
	}

	/** Get the logged in user */
	public IrisUser getUser() {
		return connection.getUser();
	}

	/** Get the selectionModel for the handler */
	public TmsSelectionModel getSelectionModel() {
		return selectionModel;
	}

	/** Add a device to the handler */
	protected void doAdd(int index, Object element) throws RemoteException {
		cacheProxy(element);
		super.doAdd(index, element);
		fireDataChanged();
	}

	/** Remove a proxy from the handler */
	protected void removeProxy(TmsMapProxy proxy) {
		synchronized(proxies) {
			proxies.remove(proxy);
		}
	}

	/** Remove a proxy from the handler by index number */
	protected Object doRemove(int index) throws RemoteException {
		Object element = super.doRemove(index);
		TmsMapProxy proxy = getProxy(element);
		removeProxy(proxy);
		fireDataChanged();
		return element;
	}

	/** Set a device state */
	protected void doSet(int index, Object element) throws RemoteException {
		super.doSet(index, element);
		TmsMapProxy proxy = getProxy(element);
		if(proxy == null) {
			System.err.println("Proxy not found: " + element);
			return;
		}
		proxy.updateStatusInfo();
		fileProxy(proxy);
		fireDataChanged();
	}

	/** Remove all of the proxies from the handler */
	protected void removeAllProxies() {
		synchronized(proxies) {
			proxies.clear();
		}
	}

	/** Remove all RefreshListeners */
	protected void removeAllRefreshListeners() {
		RefreshListener[] el = listeners.getListeners(
			RefreshListener.class);
		for(int i = 0; i < el.length; i++)
			listeners.remove(RefreshListener.class, el[i]);
	}

	/** Clean up all resources used */
	public void dispose() {
		super.dispose();
		removeAllProxies();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				removeAllRefreshListeners();
			}
		});
		selectionModel.dispose();
	}
}
