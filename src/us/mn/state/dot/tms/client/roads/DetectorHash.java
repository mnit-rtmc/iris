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
package us.mn.state.dot.tms.client.roads;

import java.util.HashMap;
import java.util.HashSet;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.R_Node;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.Session;

/**
 * A detector hash keeps track of r_node / detector links.
 *
 * @author Douglas Lau
 */
public class DetectorHash {

	/** User session */
	private final Session session;

	/** R_Node manager */
	private final R_NodeManager r_node_manager;

	/** Mapping of r_node names to detector sets */
	private final HashMap<String, HashSet<Detector>> nodes =
		new HashMap<String, HashSet<Detector>>();

	/** Listener for detector proxies */
	private final ProxyListener<Detector> listener =
		new ProxyListener<Detector>()
	{
		public void proxyAdded(final Detector proxy) {
			WORKER.addJob(new Job() {
				public void perform() {
					proxyAddedSlow(proxy);
				}
			});
		}
		public void proxyRemoved(final Detector proxy) {
			WORKER.addJob(new Job() {
				public void perform() {
					proxyRemovedSlow(proxy);
				}
			});
		}
		public void proxyChanged(Detector proxy, String a) { }
		public void enumerationComplete() {
			WORKER.addJob(new Job() {
				public void perform() {
					enumerationCompleteSlow();
					r_node_manager.arrangeCorridors();
				}
			});
		}
	};

	/** Create a new detector hash */
	public DetectorHash(Session s, R_NodeManager r_man) {
		session = s;
		r_node_manager = r_man;
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
	private void proxyAddedSlow(Detector det) {
		R_Node n = det.getR_Node();
		if(n != null)
			getDetectors(n).add(det);
	}

	/** Remove a detector from the hash */
	private void proxyRemovedSlow(Detector det) {
		R_Node n = det.getR_Node();
		if(n != null)
			getDetectors(n).remove(det);
	}

	/** Called when proxy enumeration is complete */
	private void enumerationCompleteSlow() {
		r_node_manager.arrangeCorridors();
	}

	/** Get the detectors for a specific r_node */
	public HashSet<Detector> getDetectors(R_Node n) {
		return getDetectors(n.getName());
	}

	/** Get a detector set for a node ID */
	private HashSet<Detector> getDetectors(String nid) {
		synchronized(nodes) {
			HashSet<Detector> dets = nodes.get(nid);
			if(dets == null) {
				dets = new HashSet<Detector>();
				nodes.put(nid, dets);
			}
			return dets;
		}
	}
}
