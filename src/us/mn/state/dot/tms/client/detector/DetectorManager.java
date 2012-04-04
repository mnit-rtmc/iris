/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.StyleListModel;
import us.mn.state.dot.tms.client.proxy.StyleSummary;
import us.mn.state.dot.tms.client.roads.R_NodeManager;

/**
 * A detector manager is a container for SONAR detector objects.
 *
 * @author Douglas Lau
 */
public class DetectorManager extends ProxyManager<Detector> {

	/** Shape for map object rendering */
	static protected final DetectorMarker MARKER = new DetectorMarker();

	/** Name of active style */
	static public final String STYLE_ACTIVE = "Active";

	/** Name of inactive style */
	static public final String STYLE_INACTIVE = "Inactive";

	/** Name of "no controller" style */
	static public final String STYLE_NO_CONTROLLER = "No controller";

	/** R_Node manager */
	protected final R_NodeManager r_node_manager;

	/** Create a new detector manager */
	public DetectorManager(TypeCache<Detector> c, GeoLocManager lm,
		R_NodeManager r_man)
	{
		super(c, lm);
		r_node_manager = r_man;
		cache.addProxyListener(this);
	}

	/** Create a style list model for the given symbol */
	protected StyleListModel<Detector> createStyleListModel(Symbol s) {
		return new StyleListModel<Detector>(this, s.getLabel(),
			s.getLegend());
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "Detector";
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(AffineTransform at) {
		return MARKER.createTransformedShape(at);
	}

	/** Create a theme for detectors */
	protected ProxyTheme<Detector> createTheme() {
		ProxyTheme<Detector> theme = new ProxyTheme<Detector>(this,
			getProxyType(), MARKER);
		theme.addStyle(STYLE_ACTIVE, ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(STYLE_INACTIVE, ProxyTheme.COLOR_INACTIVE,
			ProxyTheme.OUTLINE_INACTIVE);
		theme.addStyle(STYLE_NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Create a new style summary for this proxy type */
	public StyleSummary<Detector> createStyleSummary() {
		StyleSummary<Detector> summary = super.createStyleSummary();
		summary.setStyle(STYLE_ACTIVE);
		return summary;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, Detector proxy) {
		if(STYLE_ACTIVE.equals(s)) {
			Controller ctr = proxy.getController();
			return ctr != null && ctr.getActive();
		} else if(STYLE_INACTIVE.equals(s)) {
			Controller ctr = proxy.getController();
			return ctr == null || !ctr.getActive();
		} else if(STYLE_NO_CONTROLLER.equals(s))
			return proxy.getController() == null;
		else
			return STYLE_ALL.equals(s);
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		// No detector properties form
	}

	/** Create a popup menu for the selected proxy object(s) */
	protected JPopupMenu createPopup() {
		return null;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(Detector proxy) {
		return DetectorHelper.getGeoLoc(proxy);
	}

	/** Mapping of r_node names to detector sets */
	protected final HashMap<String, HashSet<Detector>> nodes =
		new HashMap<String, HashSet<Detector>>();

	/** Add a detector to the manager */
	protected void proxyAddedSlow(Detector proxy) {
		super.proxyAddedSlow(proxy);
		R_Node n = proxy.getR_Node();
		if(n != null)
			getDetectors(n).add(proxy);
	}

	/** Get the detectors for a specific r_node */
	public HashSet<Detector> getDetectors(R_Node n) {
		return getDetectors(n.getName());
	}

	/** Get a detector set for a node ID */
	protected HashSet<Detector> getDetectors(String nid) {
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
	protected void proxyRemovedSlow(Detector proxy) {
		super.proxyRemovedSlow(proxy);
		R_Node n = proxy.getR_Node();
		if(n != null)
			getDetectors(n).remove(proxy);
	}

	/** Called when proxy enumeration is complete */
	public void enumerationComplete() {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				r_node_manager.arrangeCorridors();
			}
		}.addToScheduler();
		super.enumerationComplete();
	}

	/** Get the layer zoom visibility threshold */
	protected int getZoomThreshold() {
		return 18;
	}
}
