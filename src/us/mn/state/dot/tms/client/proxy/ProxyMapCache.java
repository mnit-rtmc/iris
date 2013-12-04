/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2013  Minnesota Department of Transportation
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sonar.SonarObject;

/**
 * A cache mapping from MapGeoLoc to proxy objects.  This cache
 * is an optimization to help ProxyManager.findProxy run fast.
 *
 * @author Douglas Lau
 */
public final class ProxyMapCache<T extends SonarObject>
	implements Iterable<MapGeoLoc>
{
	/** Mapping from MapGeoLoc to proxy objects.  This is an optimization
	 * cache to help findProxy run fast. */
	private final HashMap<MapGeoLoc, T> map_proxies =
		new HashMap<MapGeoLoc, T>();

	/** Dispose of the proxy map cache */
	public synchronized void dispose() {
		map_proxies.clear();
	}

	/** Put an entry into cache.
	 * @param loc Map object to associate with proxy.
	 * @param proxy Proxy to associate with map object. */
	public synchronized void put(MapGeoLoc loc, T proxy) {
		map_proxies.put(loc, proxy);
	}

	/** Remove an entry from cache.
	 * @param proxy Proxy to remove from cache. */
	public synchronized void remove(T proxy) {
		Iterator<Map.Entry<MapGeoLoc, T>> it =
			map_proxies.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<MapGeoLoc, T> ent = it.next();
			if(ent.getValue() == proxy) {
				it.remove();
				break;
			}
		}
	}

	/** Lookup a proxy in the cache.
	 * @param loc Map object to find associated proxy.
	 * @return Proxy associated with map object. */
	public synchronized T lookup(MapGeoLoc loc) {
		return map_proxies.get(loc);
	}

	/** Get an iterator over the MapGeoLoc keys */
	public Iterator<MapGeoLoc> iterator() {
		return map_proxies.keySet().iterator();
	}
}
