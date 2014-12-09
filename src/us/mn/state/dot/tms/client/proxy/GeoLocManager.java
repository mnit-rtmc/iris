/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import static us.mn.state.dot.tms.CorridorBase.nodeDistance;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.roads.R_NodeManager;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.KILOMETERS;

/**
 * Manager for GeoLoc proxy objects.
 *
 * @author Douglas Lau
 */
public class GeoLocManager {

	/** Maximum distance from an r_node to calculate tangent angle */
	static private final Distance TANGENT_DIST = new Distance(2,KILOMETERS);

	/** User session */
	private final Session session;

	/** Map of all GeoLocs */
	private final HashMap<String, MapGeoLoc> proxies =
		new HashMap<String, MapGeoLoc>();

	/** Listener for proxy events */
	private final SwingProxyAdapter<GeoLoc> listener =
		new SwingProxyAdapter<GeoLoc>(true)
	{
		protected void proxyAddedSwing(GeoLoc proxy) {
			GeoLocManager.this.proxyAddedSwing(proxy);
		}
		protected void proxyRemovedSwing(GeoLoc proxy) {
			GeoLocManager.this.proxyRemovedSwing(proxy);
		}
		protected void proxyChangedSwing(GeoLoc proxy, String attr) {
			GeoLocManager.this.proxyChangedSwing(proxy, attr);
		}
	};

	/** Create a new GeoLoc manager */
	public GeoLocManager(Session s) {
		session = s;
	}

	/** Get the geo loc cache */
	private TypeCache<GeoLoc> getCache() {
		return session.getSonarState().getGeoLocs();
	}

	/** Initialize the GeoLoc manager */
	public void initialize() {
		getCache().addProxyListener(listener);
	}

	/** Dispose of the GeoLoc manager */
	public void dispose() {
		getCache().removeProxyListener(listener);
	}

	/** Add a new GeoLoc to the manager */
	private void proxyAddedSwing(GeoLoc proxy) {
		MapGeoLoc loc = new MapGeoLoc(proxy);
		proxies.put(proxy.getName(), loc);
	}

	/** Remove a GeoLoc from the manager */
	private void proxyRemovedSwing(GeoLoc proxy) {
		String name = proxy.getName();
		if (name != null)
			proxies.remove(name);
	}

	/** Change a proxy in the model */
	private void proxyChangedSwing(GeoLoc proxy, String attrib) {
		MapGeoLoc loc = proxies.get(proxy.getName());
		if (loc != null) {
			loc.doUpdate();
			loc.updateGeometry();
		}
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
