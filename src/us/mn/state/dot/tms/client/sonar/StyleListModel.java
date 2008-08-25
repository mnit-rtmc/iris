/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.sonar;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.event.ListDataListener;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.MapSearcher;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * A list model for device styles.
 *
 * @author Douglas lau
 */
public class StyleListModel<T extends SonarObject> extends ProxyListModel<T> {

	/** Proxy manager */
	protected final ProxyManager<T> manager;

	/** Model name */
	protected final String name;

	/** Legend icon */
	protected final Icon legend;

	/** Selection model for the style list model */
	protected final StyleListSelectionModel<T> smodel;

	/** Create a new style list model */
	public StyleListModel(ProxyManager<T> m, String n, Icon l) {
		super(m.getCache());
		manager = m;
		name = n;
		legend = l;
		smodel = new StyleListSelectionModel<T>(this, m);
	}

	/** Dispose of the list model */
	public void dispose() {
		super.dispose();
		for(ListDataListener l: getListDataListeners())
			removeListDataListener(l);
	}

	/** Add a new proxy */
	public void proxyAdded(T proxy) {
		if(manager.checkStyle(name, proxy))
			super.proxyAdded(proxy);
	}

	/** Check if the proxy is the right style after a change */
	protected int postChangeRow(T proxy) {
		if(manager.checkStyle(name, proxy))
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Get the style name */
	public String getName() {
		return name;
	}

	/** Get the style legend */
	public Icon getLegend() {
		return legend;
	}

	/** Get the list selection model */
	public StyleListSelectionModel<T> getSelectionModel() {
		return smodel;
	}

	/** Get the style name */
	public String toString() {
		return name;
	}

	/** Iterate through all proxy objects in the model */
	public MapObject forEach(MapSearcher s) {
		synchronized(proxies) {
			for(T proxy: proxies) {
				MapGeoLoc loc = manager.findGeoLoc(proxy);
				if(s.next(loc))
					return loc;
			}
		}
		return null;
	}

	/** Find a proxy using a map searcher */
	public T findProxy(MapSearcher s) {
		synchronized(proxies) {
			for(T proxy: proxies) {
				MapGeoLoc loc = manager.findGeoLoc(proxy);
				if(s.next(loc))
					return proxy;
			}
		}
		return null;
	}
}
