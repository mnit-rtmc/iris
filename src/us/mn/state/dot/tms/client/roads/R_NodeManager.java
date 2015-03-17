/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2015  Minnesota Department of Transportation
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

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import us.mn.state.dot.geokit.Position;
import us.mn.state.dot.geokit.SphericalMercatorPosition;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeTransition;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.RoadClass;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.StyleListModel;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;
import us.mn.state.dot.tms.client.widget.Invokable;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runQueued;
import us.mn.state.dot.tms.utils.I18N;

/**
 * R_Node manager provides proxies for roadway nodes.
 *
 * @author Douglas Lau
 */
public class R_NodeManager extends ProxyManager<R_Node> {

	/** Offset angle for default North map markers */
	static private final double NORTH_ANGLE = Math.PI / 2;

	/** Background color for nodes with GPS points */
	static public final Color COLOR_GPS = Color.GREEN;

	/** Background color for nodes with bad locations */
	static public final Color COLOR_NO_LOC = Color.RED;

	/** Background color for inactive nodes */
	static public final Color COLOR_INACTIVE = Color.GRAY;

	/** Marker to draw r_nodes */
	static private final R_NodeMarker MARKER = new R_NodeMarker();

	/** Map to of corridor names to corridors */
	private final Map<String, CorridorBase> corridors =
		new TreeMap<String, CorridorBase>();

	/** Combo box model of all corridors */
	private final DefaultComboBoxModel model = new DefaultComboBoxModel();

	/** Get the corridor list model */
	public DefaultComboBoxModel getCorridorModel() {
		return model;
	}

	/** Detector cache */
	private final TypeCache<Detector> det_cache;

	/** Detector proxy listener */
	private final SwingProxyAdapter<Detector> det_listener =
		new SwingProxyAdapter<Detector>()
	{
		protected void proxyAddedSwing(Detector proxy) {
			arrangeSegments(proxy);
		}
		protected void proxyRemovedSwing(Detector proxy) {
			arrangeSegments(proxy);
		}
		protected boolean checkAttributeChange(String attr) {
			return "r_node".equals(attr);
		}
		protected void proxyChangedSwing(Detector proxy, String attr) {
			/* FIXME: should arrange old corridor too */
			arrangeSegments(proxy);
		}
		protected void enumerationCompleteSwing(
			Collection<Detector> proxies)
		{
			arrangeSegments();
		}
	};

	/** Segment layer */
	private final SegmentLayer seg_layer;

	/** Currently selected corridor */
	private CorridorBase corridor;

	/** Select a new roadway corridor */
	public void setCorridor(CorridorBase c) {
		corridor = c;
	}

	/** Create a new roadway node manager */
	public R_NodeManager(Session s, GeoLocManager lm) {
		super(s, lm);
		seg_layer = new SegmentLayer(session, this);
		model.addElement(" ");
		det_cache = s.getSonarState().getDetCache().getDetectors();
	}

	/** Initialize the r_node manager */
	@Override
	public void initialize() {
		seg_layer.initialize();
		super.initialize();
		det_cache.addProxyListener(det_listener);
	}

	/** Dispose of the r_node manager */
	@Override
	public void dispose() {
		det_cache.removeProxyListener(det_listener);
		super.dispose();
		seg_layer.dispose();
	}

	/** Get the sonar type name */
	@Override
	public String getSonarType() {
		return R_Node.SONAR_TYPE;
	}

	/** Get the r_node cache */
	@Override
	public TypeCache<R_Node> getCache() {
		return session.getSonarState().getDetCache().getR_Nodes();
	}

	/** Create an r_node map tab */
	@Override
	public R_NodeTab createTab() {
		return new R_NodeTab(session, this);
	}

	/** Add a new proxy to the r_node manager */
	@Override
	protected void proxyAddedSwing(R_Node n) {
		super.proxyAddedSwing(n);
		CorridorBase c = getCorridor(n);
		if (c != null) {
			c.addNode(n);
			arrangeCorridor(c);
			arrangeSegments(c);
		}
	}

	/** Get a corridor for the specified r_node */
	public CorridorBase getCorridor(R_Node r_node) {
		GeoLoc loc = r_node.getGeoLoc();
		String cid = GeoLocHelper.getCorridorName(loc);
		if (cid != null) {
			if (corridors.containsKey(cid))
				return corridors.get(cid);
			else {
				CorridorBase c = new CorridorBase(loc);
				addCorridor(c);
				return c;
			}
		} else
			return null;
	}

	/** Add a corridor to the corridor model */
	private void addCorridor(CorridorBase c) {
		String cid = c.getName();
		corridors.put(cid, c);
		Iterator<String> it = corridors.keySet().iterator();
		for (int i = 0; it.hasNext(); i++) {
			if (cid.equals(it.next())) {
				model.insertElementAt(c, i + 1);
				return;
			}
		}
	}

	/** Called when an r_node has been removed */
	@Override
	protected void proxyRemovedSwing(R_Node n) {
		super.proxyRemovedSwing(n);
		CorridorBase c = getCorridor(n);
		if (c != null) {
			c.removeNode(n);
			arrangeCorridor(c);
			arrangeSegments(c);
		}
	}

	/** Enumeraton complete */
	@Override
	protected void enumerationCompleteSwing(Collection<R_Node> proxies) {
		super.enumerationCompleteSwing(proxies);
		for (R_Node n : proxies) {
			CorridorBase c = getCorridor(n);
			if (c != null)
				c.addNode(n);
		}
		arrangeCorridors();
	}

	/** Arrange the corridor mapping */
	private void arrangeCorridors() {
		for (final CorridorBase c : corridors.values()) {
			runQueued(new Invokable() {
				public void invoke() {
					arrangeCorridor(c);
				}
			});
		}
	}

	/** Arrange a single corridor */
	private void arrangeCorridor(CorridorBase c) {
		c.arrangeNodes();
		setTangentAngles(c);
	}

	/** Arrange the segments for all corridors */
	private void arrangeSegments() {
		for (final CorridorBase c : corridors.values()) {
			runQueued(new Invokable() {
				public void invoke() {
					arrangeSegments(c);
				}
			});
		}
	}

	/** Arrange segments in a corridor */
	private void arrangeSegments(CorridorBase c) {
		if (c.getRoadDir() > 0)
			seg_layer.updateCorridor(c);
	}

	/** Arrange segments for a detector */
	private void arrangeSegments(Detector d) {
		R_Node n = d.getR_Node();
		if (n != null) {
			CorridorBase c = getCorridor(n);
			if (c != null)
				arrangeSegments(c);
		}
	}

	/** Set the tangent angles for all the nodes in a corridor */
	private void setTangentAngles(CorridorBase c) {
		MapGeoLoc loc_a = null;		// upstream location
		MapGeoLoc loc = null;		// current location
		Iterator<MapGeoLoc> it = mapLocationIterator(c);
		while (it.hasNext()) {
			MapGeoLoc loc_b = it.next();	// downstream location
			MapGeoLoc lup = loc_a != null ? loc_a : loc;
			if (lup != null)
				setTangentAngle(loc, lup, loc_b);
			loc_a = loc;
			loc = loc_b;
		}
		// special handling for last node
		if (loc_a != null)
			setTangentAngle(loc, loc_a, loc);
	}

	/** Set the tangent angle for one location.
	 * @param loc Location to set tangent.
	 * @param loc_a Upstream location.
	 * @param loc_b Downstream loction. */
	private void setTangentAngle(MapGeoLoc loc, MapGeoLoc loc_a,
		MapGeoLoc loc_b)
	{
		double t = GeoLocHelper.calculateBearing(loc_a.getGeoLoc(),
			loc_b.getGeoLoc());
		if (!Double.isInfinite(t) && !Double.isNaN(t)) {
			loc.setTangent(t - NORTH_ANGLE);
			loc.doUpdate();
		}
	}

	/** Get the tangent angle for the given location */
	@Override
	public Double getTangentAngle(MapGeoLoc loc) {
		// tangent angle is handled specially for r_nodes
		return null;
	}

	/** Create an iterator for MapGeoLocs on a corridor.
	 * @param c Corridor.
	 * @return MapGeoLoc iterator for R_Nodes on corridor. */
	private Iterator<MapGeoLoc> mapLocationIterator(CorridorBase c) {
		final Iterator<R_Node> it = c.iterator();
		return new Iterator<MapGeoLoc>() {
			private MapGeoLoc nloc = null;
			public boolean hasNext() {
				primeLoc();
				return nloc != null;
			}
			private void primeLoc() {
				if (nloc == null)
					nloc = nextLoc();
			}
			private MapGeoLoc nextLoc() {
				while (it.hasNext()) {
					R_Node n = it.next();
					MapGeoLoc l = superFindGeoLoc(n);
					if (l != null)
						return l;
				}
				return null;
			}
			public MapGeoLoc next() {
				primeLoc();
				MapGeoLoc l = nloc;
				nloc = null;
				return l;
			}
			public void remove() { }
		};
	}

	/** Get the segment layer */
	public SegmentLayer getSegmentLayer() {
		return seg_layer;
	}

	/** Get a transformed marker shape */
	@Override
	protected Shape getShape(AffineTransform at) {
		return MARKER.createTransformedShape(at);
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, R_Node proxy) {
		switch (is) {
		case GPS:
			return !GeoLocHelper.isNull(getGeoLoc(proxy));
		case NO_LOC:
			return GeoLocHelper.isNull(getGeoLoc(proxy));
		case INACTIVE:
			return !proxy.getActive();
		case ALL:
			return true;
		default:
			return false;
		}
	}

	/** Create a style list model for the given symbol */
	@Override
	protected StyleListModel<R_Node> createStyleListModel(Symbol s) {
		// No style list models on roadway tab
		return null;
	}

	/** Create a theme for r_nodes */
	@Override
	protected R_NodeMapTheme createTheme() {
		R_NodeMapTheme theme = new R_NodeMapTheme(this);
		// order determines precidence of assigned style
		theme.addStyle(ItemStyle.INACTIVE, COLOR_INACTIVE);
		theme.addStyle(ItemStyle.GPS, COLOR_GPS);
		theme.addStyle(ItemStyle.NO_LOC, COLOR_NO_LOC);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Lookup the corridor for a location */
	public CorridorBase lookupCorridor(GeoLoc loc) {
		String cid = GeoLocHelper.getCorridorName(loc);
		if (cid != null)
			return corridors.get(cid);
		else
			return null;
	}

	/** Create a set of roadway nodes for the current corridor */
	public Set<R_Node> createSet() {
		HashSet<R_Node> nodes = new HashSet<R_Node>();
		for (R_Node n: getCache()) {
			if (checkCorridor(n))
				nodes.add(n);
		}
		return nodes;
	}

	/** Check the corridor of an r_node */
	public boolean checkCorridor(R_Node n) {
		return checkCorridor(corridor, n.getGeoLoc());
	}

	/** Check if an r_node is on the specified corridor */
	private boolean checkCorridor(CorridorBase cb, GeoLoc loc) {
		if (cb == null)
			return loc != null && loc.getRoadway() == null;
		else {
			return loc != null &&
			       cb.getRoadway() == loc.getRoadway() &&
			       cb.getRoadDir() == loc.getRoadDir();
		}
	}

	/** Create a popup menu for a single selection */
	@Override
	protected JPopupMenu createPopupSingle(R_Node proxy) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		return p;
	}

	/** Create a popup menu for multiple objects */
	@Override
	protected JPopupMenu createPopupMulti(int n_selected) {
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel(I18N.get("r_node.title") + ": " +
			n_selected));
		return p;
	}

	/** Find the map geo location for a proxy */
	@Override
	public MapGeoLoc findGeoLoc(R_Node proxy) {
		if (corridor == null || checkCorridor(proxy))
			return super.findGeoLoc(proxy);
		else
			return null;
	}

	/** Find the map geo location for a proxy */
	private MapGeoLoc superFindGeoLoc(R_Node proxy) {
		return super.findGeoLoc(proxy);
	}

	/** Get the GeoLoc for the specified proxy */
	protected GeoLoc getGeoLoc(R_Node proxy) {
		return proxy.getGeoLoc();
	}

	/** Create a GeoLoc snapped to nearest corridor */
	public GeoLoc createGeoLoc(SphericalMercatorPosition smp,
		boolean cd_road)
	{
		GeoLoc loc = null;
		double distance = Double.POSITIVE_INFINITY;
		for (CorridorBase c: corridors.values()) {
			boolean cd = RoadClass.fromOrdinal(
				c.getRoadway().getRClass()) ==RoadClass.CD_ROAD;
			if ((cd_road && !cd) || (cd && !cd_road))
				continue;
			ClientGeoLoc l = createGeoLoc(c, smp);
			if (l != null && l.getDistance() < distance) {
				loc = l;
				distance = l.getDistance();
			}
		}
		return loc;
	}

	/** Create the nearest GeoLoc for the given corridor.
	 * @param c Corridor to search.
	 * @param smp Selected point (spherical mercator position).
	 * @return ClientGeoLoc snapped to corridor, or null if not found. */
	private ClientGeoLoc createGeoLoc(CorridorBase c,
		SphericalMercatorPosition smp)
	{
		R_Node n0 = null;
		R_Node n1 = null;
		R_Node n_prev = null;
		double n_meters = Double.POSITIVE_INFINITY;
		for (R_Node n: c) {
			if (isContinuityBreak(n)) {
				n_prev = null;
				continue;
			}
			if (n_prev != null) {
				double m = calcDistance(n_prev, n, smp);
				if (m < n_meters) {
					n0 = n_prev;
					n1 = n;
					n_meters = m;
				}
			}
			n_prev = n;
		}
		if (n0 != null)
			return createGeoLoc(n0, n1, smp, n_meters);
		else
			return null;
	}

	/** Check if a given node is a continuity break */
	private boolean isContinuityBreak(R_Node n) {
		if (n.getNodeType() == R_NodeType.ACCESS.ordinal())
			return true;
		if (n.getTransition() == R_NodeTransition.COMMON.ordinal())
			return true;
		return false;
	}

	/** Calculate the distance from a point to the given line segment.
	 * @param n0 First r_node
	 * @param n1 Second (adjacent) r_node.
	 * @param smp Selected point (spherical mercator position).
	 * @return Distance (spherical mercator "meters") from segment to
	 *         selected point. */
	private double calcDistance(R_Node n0, R_Node n1,
		SphericalMercatorPosition smp)
	{
		GeoLoc l0 = n0.getGeoLoc();
		GeoLoc l1 = n1.getGeoLoc();
		return GeoLocHelper.segmentDistance(l0, l1, smp);
	}

	/** Create a GeoLoc projected onto the line between two nodes.
	 * @param n0 First node.
	 * @param n1 Second (adjacent) node.
	 * @param smp Selected point (spherical mercator position).
	 * @param d Distance (meters).
	 * @return ClientGeoLoc snapped to corridor, or null if not found. */
	private ClientGeoLoc createGeoLoc(R_Node n0, R_Node n1,
		SphericalMercatorPosition smp, double dist)
	{
		GeoLoc l0 = n0.getGeoLoc();
		GeoLoc l1 = n1.getGeoLoc();
		SphericalMercatorPosition pos = GeoLocHelper.segmentSnap(l0,
			l1, smp);
		if (pos != null) {
			Position p = pos.getPosition();
			float lat = (float)p.getLatitude();
			float lon = (float)p.getLongitude();
			return new ClientGeoLoc(l0.getRoadway(),
				l0.getRoadDir(), lat, lon, dist);
		} else
			return null;
	}

	/** Get the layer zoom visibility threshold */
	@Override
	protected int getZoomThreshold() {
		return 18;
	}
}
