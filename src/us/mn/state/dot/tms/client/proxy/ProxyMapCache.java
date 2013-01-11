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
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.sonar.SonarObject;

/**
 * A cache mapping from MapObject identityHashCode to proxy objects.  This cache
 * is an optimization to help ProxyManager.findProxy run fast.
 *
 * @author Douglas Lau
 */
public final class ProxyMapCache<T extends SonarObject> {

	/** Mapping from MapObject identityHashCode to proxy objects.  This is
	 * an optimization cache to help findProxy run fast. */
	private final HashMap<Integer, T> map_proxies =
		new HashMap<Integer, T>();

	/** Dispose of the proxy map cache */
	public void dispose() {
		map_proxies.clear();
	}

	/** Put an entry into cache.
	 * @param mo Map object to associate with proxy.
	 * @param proxy Proxy to associate with map object. */
	public synchronized void put(MapObject mo, T proxy) {
		int i = System.identityHashCode(mo);
		map_proxies.put(i, proxy);
	}

	/** Remove an entry from cache.
	 * @param proxy Proxy to remove from cache. */
	public synchronized void remove(T proxy) {
		Iterator<Map.Entry<Integer, T>> it =
			map_proxies.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<Integer, T> ent = it.next();
			if(ent.getValue() == proxy) {
				it.remove();
				break;
			}
		}
	}

	/** Lookup a proxy in the cache.
	 * @param mo Map object to find associated proxy.
	 * @return Proxy associated with map object. */
	public synchronized T lookup(MapObject mo) {
		int i = System.identityHashCode(mo);
		return map_proxies.get(i);
	}
}
