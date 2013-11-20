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
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import static us.mn.state.dot.tms.CorridorBase.nodeDistance;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.R_Node;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.roads.R_NodeManager;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.KILOMETERS;

/**
 * Manager for GeoLoc proxy objects.
 *
 * @author Douglas Lau
 */
public class GeoLocManager implements ProxyListener<GeoLoc> {

	/** Maximum distance from an r_node to calculate tangent angle */
	static private final Distance TANGENT_DIST = new Distance(2,KILOMETERS);

	/** User session */
	private final Session session;

	/** Map of all GeoLocs */
	private final HashMap<String, MapGeoLoc> proxies =
		new HashMap<String, MapGeoLoc>();

	/** Create a new GeoLoc manager */
	public GeoLocManager(Session s) {
		session = s;
		getCache().addProxyListener(this);
	}

	/** Get the geo loc cache */
	private TypeCache<GeoLoc> getCache() {
		return session.getSonarState().getGeoLocs();
	}

	/** Dispose of the proxy model */
	public void dispose() {
		getCache().removeProxyListener(this);
	}

	/** Add a new GeoLoc to the manager */
	public void proxyAdded(final GeoLoc proxy) {
		// Don't hog the SONAR TaskProcessor thread
		WORKER.addJob(new Job() {
			public void perform() {
				MapGeoLoc loc = new MapGeoLoc(proxy);
				synchronized(proxies) {
					proxies.put(proxy.getName(), loc);
				}
			}
		});
	}

	/** Enumeration of the proxy type has completed */
	public void enumerationComplete() {
		// We're not interested
	}

	/** Remove a GeoLoc from the manager */
	public void proxyRemoved(GeoLoc proxy) {
		// Get the name before the proxy is destroyed
		final String name = proxy.getName();
		// Don't hog the SONAR TaskProcessor thread
		WORKER.addJob(new Job() {
			public void perform() {
				synchronized(proxies) {
					proxies.remove(name);
				}
			}
		});
	}

	/** Change a proxy in the model */
	public void proxyChanged(final GeoLoc proxy, String attrib) {
		// Don't hog the SONAR TaskProcessor thread
		WORKER.addJob(new Job() {
			public void perform() {
				MapGeoLoc loc;
				synchronized(proxies) {
					loc = proxies.get(proxy.getName());
				}
				if(loc != null) {
					loc.doUpdate();
					loc.updateGeometry();
				}
			}
		});
	}

	/** Find the map location for the given proxy */
	public MapGeoLoc findMapGeoLoc(GeoLoc proxy) {
		synchronized(proxies) {
			return proxies.get(proxy.getName());
		}
	}

	/** Get the tangent angle for a location */
	public Double getTangentAngle(MapGeoLoc mloc) {
		R_NodeManager n_man = session.getR_NodeManager();
		if(n_man != null)
			return getTangentAngle(n_man, mloc);
		else
			return null;
	}

	/** Get the tangent angle for a location */
	private Double getTangentAngle(R_NodeManager n_man, MapGeoLoc mloc) {
		GeoLoc loc = mloc.getGeoLoc();
		CorridorBase c = n_man.lookupCorridor(loc);
		if(c != null) {
			R_Node r_node = c.findNearest(loc);
			if(r_node != null &&
			   nodeDistance(r_node, loc).m() < TANGENT_DIST.m())
			{
				MapGeoLoc n_loc = n_man.findGeoLoc(r_node);
				if(n_loc != null)
					return n_loc.getTangent();
			}
		}
		return null;
	}
}
