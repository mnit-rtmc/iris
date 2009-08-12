/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2009  Minnesota Department of Transportation
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.DefaultListModel;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import us.mn.state.dot.map.Layer;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.StyleListModel;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * R_Node manager provides proxies for roadway nodes.
 *
 * @author Douglas Lau
 */
public class R_NodeManager extends ProxyManager<R_Node> {

	/** Offset angle for default North map markers */
	static protected final double NORTH_ANGLE = Math.PI / 2;

	/** Name of "has GPS" style */
	static public final String STYLE_GPS = "Has GPS";

	/** Name of "no GPS" style */
	static public final String STYLE_NO_GPS = "No GPS";

	/** Name of "no location" style */
	static public final String STYLE_NO_LOC = "No Location";

	/** Map to of corridor names to corridors */
	protected final Map<String, CorridorBase> corridors =
		new TreeMap<String, CorridorBase>();

	/** Map to hold exit/entrance links */
	protected final Map<String, R_Node> e_links =
		new HashMap<String, R_Node>();

	/** List model of all corridors */
	protected final DefaultListModel model = new DefaultListModel();

	/** Get the corridor list model */
	public ListModel getCorridorModel() {
		return model;
	}

	/** User session */
	protected final Session session;

	/** Segment layer */
	protected SegmentLayer seg_layer = null;

	/** Currently selected corridor */
	protected String corridor = "";

	/** Select a new freeway corridor */
	public void setCorridor(String c) {
		corridor = c;
	}

	/** Create a new roadway node manager */
	public R_NodeManager(Session s, TypeCache<R_Node> c,
		GeoLocManager lm)
	{
		super(c, lm);
		session = s;
		initialize();
	}

	/** Add a new proxy to the r_node manager */
	protected void proxyAddedSlow(R_Node n) {
		super.proxyAddedSlow(n);
		addCorridor(n);
	}

	/** Called when proxy enumeration is complete */
	public void enumerationComplete() {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				arrangeCorridors();
				createSegmentLayer();
			}
		}.addToScheduler();
	}

	/** Arrange the corridor mapping */
	protected void arrangeCorridors() {
		R_NodeHelper.find(new Checker<R_Node>() {
			public boolean check(R_Node r_node) {
				findDownstreamLinks(r_node);
				return false;
			}
		});
		for(CorridorBase c: corridors.values()) {
			c.arrangeNodes();
			setTangentAngles(c);
		}
	}

	/** Set the tangent angles for all the nodes in a corridor */
	protected void setTangentAngles(CorridorBase c) {
		LinkedList<MapGeoLoc> locs = new LinkedList<MapGeoLoc>();
		for(R_Node n: c.getNodes()) {
			MapGeoLoc loc = super.findGeoLoc(n);
			if(loc != null)
				locs.add(loc);
		}
		if(locs.size() > 0) {
			// The first and last locations need to be in the list
			// twice to allow tangents to be calculated.
			locs.addFirst(locs.getFirst());
			locs.addLast(locs.getLast());
		}
		MapGeoLoc loc_a = null;
		MapGeoLoc loc = null;
		for(MapGeoLoc loc_b: locs) {
			if(loc_a != null) {
				Vector va = Vector.create(loc_a.getGeoLoc());
				Vector vb = Vector.create(loc_b.getGeoLoc());
				Vector a = vb.subtract(va);
				double t = a.getAngle();
				if(!Double.isInfinite(t) && !Double.isNaN(t))
					loc.setTangent(t - NORTH_ANGLE);
			}
			loc_a = loc;
			loc = loc_b;
		}
	}

	/** Find downstream links (not in corridor) for the given node */
	protected void findDownstreamLinks(R_Node r_node) {
		if(R_NodeHelper.isExit(r_node))
			linkExitToEntrance(r_node);
	}

	/** Link an exit node with a corresponding entrance node */
	protected void linkExitToEntrance(final R_Node r_node) {
		final LinkedList<R_Node> nl = new LinkedList<R_Node>();
		R_NodeHelper.find(new Checker<R_Node>() {
			public boolean check(R_Node other) {
				if(R_NodeHelper.isExitLink(r_node, other))
					nl.add(other);
				return false;
			}
		});
		R_Node link = findNearest(r_node, nl);
		if(link != null) {
			e_links.put(r_node.getName(), link);
			e_links.put(link.getName(), r_node);
		}
	}

	/** Find the nearest r_node in a list */
	static protected R_Node findNearest(R_Node r_node, List<R_Node> others){
		R_Node nearest = null;
		double distance = 0;
		for(R_Node other: others) {
			double m = CorridorBase.metersTo(r_node, other);
			if(nearest == null || m < distance) {
				nearest = other;
				distance = m;
			}
		}
		return nearest;
	}

	/** Create the segment layer */
	protected synchronized void createSegmentLayer() {
		seg_layer = new SegmentLayer(this, session);
		for(CorridorBase c: corridors.values())
			seg_layer.addCorridor(c);
		notify();
	}

	/** Get the segment layer */
	public synchronized SegmentLayer getSegmentLayer() {
		while(seg_layer == null) {
			try {
				wait();
			}
			catch(InterruptedException e) { }
		}
		return seg_layer;
	}

	/** Get the proxy type */
	public String getProxyType() {
		return "R_Node";
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, R_Node proxy) {
		if(STYLE_GPS.equals(s))
			return GeoLocHelper.hasGPS(getGeoLoc(proxy));
		else if(STYLE_NO_GPS.equals(s))
			return !GeoLocHelper.hasGPS(getGeoLoc(proxy));
		else if(STYLE_NO_LOC.equals(s))
			return GeoLocHelper.isNull(getGeoLoc(proxy));
		else if(corridors.containsKey(s)) {
			String c=GeoLocHelper.getCorridorName(getGeoLoc(proxy));
			return s.equals(c);
		} else
			return STYLE_ALL.equals(s);
	}

	/** Create a style list model for the given symbol */
	protected StyleListModel<R_Node> createStyleListModel(Symbol s) {
		// No style list models on roadway tab
		return null;
	}

	/** Create a styled theme for r_nodes */
	protected StyledTheme createTheme() {
		R_NodeMapTheme theme = new R_NodeMapTheme(this);
		theme.addStyle(STYLE_GPS, R_NodeRenderer.COLOR_GPS);
		theme.addStyle(STYLE_NO_GPS, R_NodeRenderer.COLOR_NO_GPS);
		theme.addStyle(STYLE_NO_LOC, R_NodeRenderer.COLOR_NO_LOC);
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Add a corridor for the specified r_node */
	protected void addCorridor(R_Node r_node) {
		GeoLoc loc = r_node.getGeoLoc();
		String cid = GeoLocHelper.getCorridorName(loc);
		if(cid != null) {
			if(!corridors.containsKey(cid))
				addCorridor(new CorridorBase(loc, false));
			CorridorBase c = corridors.get(cid);
			if(c != null)
				c.addNode(r_node);
		}
	}

	/** Add a corridor to the corridor model */
	protected void addCorridor(CorridorBase c) {
		String cid = c.getName();
		corridors.put(cid, c);
		Iterator<String> it = corridors.keySet().iterator();
		for(int i = 0; it.hasNext(); i++) {
			if(cid.equals(it.next())) {
				model.add(i, cid);
				return;
			}
		}
	}

	/** Create a set of roadway nodes for the current corridor */
	public Set<R_Node> createSet() {
		final HashSet<R_Node> nodes = new HashSet<R_Node>();
		findCorridor(new Checker<R_Node>() {
			public boolean check(R_Node n) {
				nodes.add(n);
				return false;
			}
		});
		return nodes;
	}

	/** Find all r_nodes on the specified corridor */
	public R_Node findCorridor(final Checker<R_Node> ch) {
		return cache.findObject(new Checker<R_Node>() {
			public boolean check(R_Node n) {
				if(checkCorridor(n))
					return ch.check(n);
				else
					return false;
			}
		});
	}

	/** Check the corridor of an r_node */
	public boolean checkCorridor(R_Node n) {
		String c = GeoLocHelper.getCorridorName(n.getGeoLoc());
		if(c != null)
			return c.equals(corridor);
		else
			return "".equals(corridor);
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		if(s_model.getSelectedCount() == 1) {
			for(R_Node n: s_model.getSelected())
				showPropertiesForm(n);
		}
	}

	/** Show the properties form for the given proxy */
	protected void showPropertiesForm(R_Node n) {
		SmartDesktop desktop = session.getDesktop();
		try {
			desktop.show(new R_NodeProperties(session, n));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/** Create a popup menu for the selected proxy object(s) */
	protected JPopupMenu createPopup() {
		int n_selected = s_model.getSelectedCount();
		if(n_selected < 1)
			return null;
		if(n_selected == 1) {
			for(R_Node n: s_model.getSelected())
				return createSinglePopup(n);
		}
		JPopupMenu p = new JPopupMenu();
		p.add(new javax.swing.JLabel("" + n_selected + " R_Nodes"));
		return p;
	}

	/** Create a popup menu for a single selection */
	protected JPopupMenu createSinglePopup(R_Node proxy) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		p.addSeparator();
		p.add(new PropertiesAction<R_Node>(proxy) {
			protected void do_perform() {
				showPropertiesForm();
			}
		});
		return p;
	}

	/** Find the map geo location for a proxy */
	public MapGeoLoc findGeoLoc(R_Node proxy) {
		if("".equals(corridor) || checkCorridor(proxy))
			return super.findGeoLoc(proxy);
		else
			return null;
	}

	/** Get the GeoLoc for the specified proxy */
	protected GeoLoc getGeoLoc(R_Node proxy) {
		return proxy.getGeoLoc();
	}
}
