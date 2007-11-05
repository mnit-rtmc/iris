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
package us.mn.state.dot.tms.client.device;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.SwingUtilities;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.proxy.ProxyHandlerImpl;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;

/**
 * The DeviceHandler class provides proxies for TrafficDevice objects.  And
 * manages ListModels of proxies.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
abstract public class DeviceHandlerImpl extends ProxyHandlerImpl
	implements DeviceHandler
{
	/** Remote TMS object list */
	protected final SortedList r_list;

	/** Theme for the traffic device type */
	protected final TrafficDeviceTheme theme;

	/** Create a new device handler */
	public DeviceHandlerImpl(TmsConnection c, SortedList l,
		TrafficDeviceTheme t) throws RemoteException
	{
		super(c, l, t);
		r_list = l;
		theme = t;
	}

	/** List of all status models */
	protected final LinkedList<NamedListModel> models =
		new LinkedList<NamedListModel>();

	/** Map of status codes to status list models */
	protected final Map<Integer, NamedListModel> mod_map =
		new HashMap<Integer, NamedListModel>();

	/** Add a status model */
	protected void addStatusModel(int status) {
		Symbol s = theme.getSymbol(status);
		NamedListModel m = new NamedListModel(status, s.getLabel(),
			s.getLegend());
		models.add(m);
		synchronized(mod_map) {
			mod_map.put(status, m);
		}
	}

	/** Get an array of status list models */
	public NamedListModel[] getListModels() {
		return (NamedListModel[])models.toArray(new NamedListModel[0]);
	}

	/** Get the status list model for the specified status code */
	public NamedListModel getStatusModel(int status) {
		synchronized(mod_map) {
			return mod_map.get(new Integer(status));
		}
	}

	/** Get the status model where the proxy is currently filed */
	protected NamedListModel getCurrentModel(TrafficDeviceProxy proxy) {
		for(NamedListModel m: getListModels()) {
			synchronized(m) {
				if(m.contains(proxy))
					return m;
			}
		}
		return null;
	}

	/** Get the status list model for the specified traffic device */
	protected NamedListModel getStatusModel(TrafficDeviceProxy proxy) {
		return getStatusModel(proxy.getStatusCode());
	}

	/** File the proxy in the approriate list models */
	protected void fileProxy(final TmsMapProxy proxy) {
		TrafficDeviceProxy device = (TrafficDeviceProxy)proxy;
		final NamedListModel m = getCurrentModel(device);
		final NamedListModel model = getStatusModel(device);
		if(model != m) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if(m != null) {
						synchronized(m) {
							m.removeElement(proxy);
						}
					}
					if(model != null) {
						synchronized(model) {
							model.addElement(proxy);
						}
					}
				}
			});
		}
	}

	/** Clean up all resources used */
	public void dispose() {
		super.dispose();
		for(NamedListModel m: getListModels())
			m.dispose();
	}

	/** Remove a proxy from the handler */
	protected void removeProxy(final TmsMapProxy proxy) {
		final NamedListModel[] models = getListModels();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for(NamedListModel m: models) {
					synchronized(m) {
						m.removeElement(proxy);
					}
				}
			}
		});
		super.removeProxy(proxy);
	}

	/** Remove all of the proxies from the handler */
	protected void removeAllProxies() {
		for(NamedListModel m: getListModels())
			m.removeAllElements();
		super.removeAllProxies();
	}
}
