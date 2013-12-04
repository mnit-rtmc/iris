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
package us.mn.state.dot.tms.client.detector;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JPopupMenu;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.R_Node;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.roads.R_NodeManager;

/**
 * A detector manager is a container for SONAR detector objects.
 * FIXME: this really shouldn't be a ProxyManager, since it doesn't do the
 *        same thing as other ProxyManager subclasses.
 *
 * @author Douglas Lau
 */
public class DetectorManager extends ProxyManager<Detector> {

	/** R_Node manager */
	private final R_NodeManager r_node_manager;

	/** Create a new detector manager */
	public DetectorManager(Session s, GeoLocManager lm, R_NodeManager r_man)
	{
		super(s, lm, ItemStyle.ACTIVE);
		r_node_manager = r_man;
		getCache().addProxyListener(this);
	}

	/** Get the proxy type name */
	@Override
	public String getProxyType() {
		return "detector";
	}

	/** Get the detector cache */
	@Override
	public TypeCache<Detector> getCache() {
		return session.getSonarState().getDetCache().getDetectors();
	}

	/** Get the shape for a given proxy */
	@Override
	protected Shape getShape(AffineTransform at) {
		return null;
	}

	/** Create a theme for detectors */
	@Override
	protected ProxyTheme<Detector> createTheme() {
		return null;
	}

	/** Create a popup menu for the selected proxy object(s) */
	@Override
	protected JPopupMenu createPopup() {
		return null;
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(Detector proxy) {
		return DetectorHelper.getGeoLoc(proxy);
	}

	/** Mapping of r_node names to detector sets */
	private final HashMap<String, HashSet<Detector>> nodes =
		new HashMap<String, HashSet<Detector>>();

	/** Add a detector to the manager */
	@Override
	protected void proxyAddedSlow(Detector proxy) {
		// Don't call super.proxyAddedSlow ...
		R_Node n = proxy.getR_Node();
		if(n != null)
			getDetectors(n).add(proxy);
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

	/** Called when a detector has been removed */
	@Override
	protected void proxyRemovedSlow(Detector proxy) {
		// Don't call super.proxyRemovedSlow
		R_Node n = proxy.getR_Node();
		if(n != null)
			getDetectors(n).remove(proxy);
	}

	/** Called when proxy enumeration is complete */
	@Override
	public void enumerationComplete() {
		// Don't hog the SONAR TaskProcessor thread
		WORKER.addJob(new Job() {
			public void perform() {
				r_node_manager.arrangeCorridors();
			}
		});
		super.enumerationComplete();
	}

	/** Get the layer zoom visibility threshold */
	@Override
	protected int getZoomThreshold() {
		return 18;
	}
}
