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

import java.util.HashMap;
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

	/** Mapping from MapObject identityHashCode to proxy objects.  This is
	 * an optimization cache to help findProxy run fast. */
	protected final HashMap<Integer, T> map_proxies =
		new HashMap<Integer, T>();

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
	protected int doProxyAdded(T proxy) {
		if(manager.checkStyle(name, proxy)) {
			putMapProxy(proxy);
			return super.doProxyAdded(proxy);
		} else
			return -1;
	}

	/** Put a map proxy */
	protected void putMapProxy(T proxy) {
		// FIXME: this will leak when proxy objects are removed.
		// Not easy to fix, since proxy objects die before we can
		// get the corresponding MapGeoLoc to remove.
		MapGeoLoc loc = manager.findGeoLoc(proxy);
		if(loc != null) {
			int i = System.identityHashCode(loc);
			synchronized(map_proxies) {
				map_proxies.put(i, proxy);
			}
		} else
			System.err.println("putMapProxy failed: " + proxy);
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

	/** Find a proxy matching the given map object */
	public T findProxy(final MapObject o) {
		int i = System.identityHashCode(o);
		synchronized(map_proxies) {
			return map_proxies.get(i);
		}
	}
}
