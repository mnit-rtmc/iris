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
package us.mn.state.dot.tms.client.roads;

import java.util.HashMap;
import java.util.HashSet;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;

/**
 * A detector hash keeps track of r_node / detector links.
 *
 * @author Douglas Lau
 */
public class DetectorHash {

	/** User session */
	private final Session session;

	/** Mapping of r_node names to detector sets */
	private final HashMap<String, HashSet<Detector>> nodes =
		new HashMap<String, HashSet<Detector>>();

	/** Listener for detector proxies */
	private final SwingProxyAdapter<Detector> listener =
		new SwingProxyAdapter<Detector>(true)
	{
		protected void proxyAddedSwing(Detector proxy) {
			DetectorHash.this.proxyAddedSwing(proxy);
		}
		protected void proxyRemovedSwing(Detector proxy) {
			DetectorHash.this.proxyRemovedSwing(proxy);
		}
		protected boolean checkAttributeChange(String attr) {
			return false;
		}
	};

	/** Create a new detector hash */
	public DetectorHash(Session s) {
		session = s;
	}

	/** Initialize the detector hash */
	public void initialize() {
		getCache().addProxyListener(listener);
	}

	/** Dispose of the detector hash */
	public void dispose() {
		getCache().removeProxyListener(listener);
	}

	/** Get the detector cache */
	private TypeCache<Detector> getCache() {
		return session.getSonarState().getDetCache().getDetectors();
	}

	/** Add a detector to the hash */
	private void proxyAddedSwing(Detector det) {
		R_Node n = det.getR_Node();
		if (n != null)
			getDetectors(n).add(det);
	}

	/** Remove a detector from the hash */
	private void proxyRemovedSwing(Detector det) {
		R_Node n = det.getR_Node();
		if (n != null)
			getDetectors(n).remove(det);
	}

	/** Get the detectors for a specific r_node */
	public HashSet<Detector> getDetectors(R_Node n) {
		return getDetectors(n.getName());
	}

	/** Get a detector set for a node ID */
	private HashSet<Detector> getDetectors(String nid) {
		HashSet<Detector> dets = nodes.get(nid);
		if (dets == null) {
			dets = new HashSet<Detector>();
			nodes.put(nid, dets);
		}
		return dets;
	}
}
