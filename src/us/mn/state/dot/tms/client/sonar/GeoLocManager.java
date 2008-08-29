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
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GeoLoc;

/**
 * Manager for GeoLoc proxy objects.
 *
 * @author Douglas Lau
 */
public class GeoLocManager implements ProxyListener<GeoLoc> {

	/** Proxy type cache */
	protected final TypeCache<GeoLoc> cache;

	/** Map of all GeoLocs */
	protected final HashMap<String, MapGeoLoc> proxies =
		new HashMap<String, MapGeoLoc>();

	/** Create a new GeoLoc manager */
	public GeoLocManager(TypeCache<GeoLoc> c) {
		cache = c;
		cache.addProxyListener(this);
	}

	/** Dispose of the proxy model */
	public void dispose() {
		cache.removeProxyListener(this);
	}

	/** Add a new GeoLoc to the manager */
	public void proxyAdded(final GeoLoc proxy) {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				MapGeoLoc loc = new MapGeoLoc(proxy);
				synchronized(proxies) {
					proxies.put(proxy.getName(), loc);
				}
			}
		}.addToScheduler();
	}

	/** Remove a GeoLoc from the manager */
	public void proxyRemoved(GeoLoc proxy) {
		// Get the name before the proxy is destroyed
		final String name = proxy.getName();
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				synchronized(proxies) {
					proxies.remove(name);
				}
			}
		}.addToScheduler();
	}

	/** Change a proxy in the model */
	public void proxyChanged(final GeoLoc proxy, String attrib) {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				MapGeoLoc loc;
				synchronized(proxies) {
					loc = proxies.get(proxy.getName());
				}
				if(loc != null)
					loc.doUpdate();
			}
		}.addToScheduler();
	}

	/** Find the map location for the given proxy */
	public MapGeoLoc findMapGeoLoc(GeoLoc proxy) {
		synchronized(proxies) {
			return proxies.get(proxy.getName());
		}
	}
}
