/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2016  Minnesota Department of Transportation
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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.map.LayerState;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;
import us.mn.state.dot.tms.client.proxy.WorkRequestAction;
import us.mn.state.dot.tms.client.widget.Invokable;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runQueued;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * R_Node manager provides proxies for roadway nodes.
 *
 * @author Douglas Lau
 */
public class R_NodeManager extends ProxyManager<R_Node> {

	/** Create a proxy descriptor */
	static private ProxyDescriptor<R_Node> descriptor(Session s) {
		return new ProxyDescriptor<R_Node>(
			s.getSonarState().getDetCache().getR_Nodes(),
			false
		);
	}

	/** Offset angle for default North map markers */
	static private final double NORTH_ANGLE = Math.PI / 2;

	/** Maximum distance to snap */
	static private final Distance MAX_DIST = new Distance(1, MILES);

	/** Map to of corridor names to corridors */
	private final Map<String, CorridorBase<R_Node>> corridors =
		new TreeMap<String, CorridorBase<R_Node>>();

	/** Combo box model of all corridors */
	private final DefaultComboBoxModel<CorridorBase<R_Node>> cor_mdl =
		new DefaultComboBoxModel<CorridorBase<R_Node>>();

	/** Get the corridor list model */
	public ComboBoxModel<CorridorBase<R_Node>> getCorridorModel() {
		return cor_mdl;
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

	/** Segment builder */
	private final SegmentBuilder builder;

	/** Create a new roadway node manager */
	public R_NodeManager(Session s, GeoLocManager lm, Properties p)
		throws IOException, SAXException, ParserConfigurationException
	{
		super(s, lm, descriptor(s), 10);
		builder = canRead()
		       ? new SegmentBuilder(session, this, p)
		       : null;
		cor_mdl.addElement(null);
		det_cache = s.getSonarState().getDetCache().getDetectors();
	}

	/** Initialize the r_node manager */
	@Override
	public void initialize() {
		super.initialize();
		if (builder != null) {
			builder.initialize();
			det_cache.addProxyListener(det_listener);
		} else
			logErr("NO SegmentBuilder!");
	}

	/** Log an error to stderr */
	private void logErr(String msg) {
		System.err.println("R_NodeManager " + msg);
	}

	/** Dispose of the r_node manager */
	@Override
	public void dispose() {
		if (builder != null) {
			det_cache.removeProxyListener(det_listener);
			builder.dispose();
		}
		super.dispose();
	}

	/** Create an r_node map tab */
	@Override
	public R_NodeTab createTab() {
		return new R_NodeTab(session, this);
	}

	/** Create layer state for a map bean */
	@Override
	public LayerState createState(MapBean mb) {
		return new SegmentLayerState(this, getLayer(), mb, builder);
	}

	/** Add a new proxy to the r_node manager */
	@Override
	protected void proxyAddedSwing(R_Node n) {
		super.proxyAddedSwing(n);
		CorridorBase<R_Node> c = getCorridor(n);
		if (c != null) {
			c.addNode(n);
			arrangeCorridor(c);
			arrangeSegments(c);
		}
	}

	/** Get a corridor for the specified r_node */
	public CorridorBase<R_Node> getCorridor(R_Node r_node) {
		GeoLoc loc = r_node.getGeoLoc();
		String cid = GeoLocHelper.getCorridorName(loc);
		if (cid != null) {
			if (corridors.containsKey(cid))
				return corridors.get(cid);
			else {
				CorridorBase<R_Node> c = new CorridorBase
					<R_Node>(loc);
				addCorridor(c);
				return c;
			}
		} else
			return null;
	}

	/** Add a corridor to the corridor model */
	private void addCorridor(CorridorBase<R_Node> c) {
		String cid = c.getName();
		corridors.put(cid, c);
		Iterator<String> it = corridors.keySet().iterator();
		for (int i = 0; it.hasNext(); i++) {
			if (cid.equals(it.next())) {
				cor_mdl.insertElementAt(c, i + 1);
				return;
			}
		}
	}

	/** Called when an r_node has been removed */
	@Override
	protected void proxyRemovedSwing(R_Node n) {
		super.proxyRemovedSwing(n);
		CorridorBase<R_Node> c = getCorridor(n);
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
			CorridorBase<R_Node> c = getCorridor(n);
			if (c != null)
				c.addNode(n);
		}
		arrangeCorridors();
	}

	/** Arrange the corridor mapping */
	private void arrangeCorridors() {
		for (final CorridorBase<R_Node> c : corridors.values()) {
			runQueued(new Invokable() {
				public void invoke() {
					arrangeCorridor(c);
				}
			});
		}
	}

	/** Arrange a single corridor */
	private void arrangeCorridor(CorridorBase<R_Node> c) {
		c.arrangeNodes();
		setTangentAngles(c);
	}

	/** Arrange the segments for all corridors */
	private void arrangeSegments() {
		for (final CorridorBase<R_Node> c : corridors.values()) {
			runQueued(new Invokable() {
				public void invoke() {
					arrangeSegments(c);
				}
			});
		}
	}

	/** Arrange segments in a corridor */
	private void arrangeSegments(CorridorBase<R_Node> c) {
		if (c.getRoadDir() > 0)
			builder.updateCorridor(c);
	}

	/** Arrange segments for a detector */
	private void arrangeSegments(Detector d) {
		R_Node n = d.getR_Node();
		if (n != null) {
			CorridorBase<R_Node> c = getCorridor(n);
			if (c != null)
				arrangeSegments(c);
		}
	}

	/** Set the tangent angles for all the nodes in a corridor */
	private void setTangentAngles(CorridorBase<R_Node> c) {
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
	private Iterator<MapGeoLoc> mapLocationIterator(CorridorBase<R_Node> c){
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
					MapGeoLoc l = findGeoLoc(n);
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

	/** Create a theme for r_nodes */
	@Override
	protected ProxyTheme<R_Node> createTheme() {
		return new ProxyTheme<R_Node>(this, new R_NodeMarker());
	}

	/** Lookup the corridor for a location */
	public CorridorBase<R_Node> lookupCorridor(GeoLoc loc) {
		String cid = GeoLocHelper.getCorridorName(loc);
		if (cid != null)
			return corridors.get(cid);
		else
			return null;
	}

	/** Get the GeoLoc for the specified proxy */
	protected GeoLoc getGeoLoc(R_Node proxy) {
		return proxy.getGeoLoc();
	}

	/** Create a GeoLoc snapped to nearest r_node segment.
	 * NOTE: copied to server/CorridorManager. */
	public GeoLoc snapGeoLoc(SphericalMercatorPosition smp, LaneType lt) {
		GeoLoc loc = null;
		Distance dist = MAX_DIST;
		for (CorridorBase<R_Node> c: corridors.values()) {
			CorridorBase.GeoLocDist ld = c.snapGeoLoc(smp, lt,dist);
			if (ld != null && ld.dist.m() < dist.m()) {
				loc = ld.loc;
				dist = ld.dist;
			}
		}
		return loc;
	}

	/** Check if user can read r_nodes / detectors */
	@Override
	public boolean canRead() {
		return super.canRead() && session.canRead(Detector.SONAR_TYPE);
	}

	/** Fill single selection popup */
	@Override
	protected void fillPopupSingle(JPopupMenu p, R_Node n) {
		for (Detector d : lookupDets(n))
			p.add(new WorkRequestAction<Detector>(d,n.getGeoLoc()));
		p.addSeparator();
	}

	/** Lookup all detectors in an r_node */
	private TreeSet<Detector> lookupDets(R_Node n) {
		TreeSet<Detector> set = new TreeSet<Detector>(
			new NumericAlphaComparator<Detector>());
		Iterator<Detector> it = DetectorHelper.iterator();
		while (it.hasNext()) {
			Detector d = it.next();
			if (d.getR_Node() == n)
				set.add(d);
		}
		return set;
	}
}
