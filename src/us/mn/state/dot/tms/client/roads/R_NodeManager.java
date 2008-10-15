/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2008  Minnesota Department of Transportation
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultListModel;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.sonar.GeoLocManager;
import us.mn.state.dot.tms.client.sonar.MapGeoLoc;
import us.mn.state.dot.tms.client.sonar.PropertiesAction;
import us.mn.state.dot.tms.client.sonar.ProxyManager;
import us.mn.state.dot.tms.client.sonar.SonarLayer;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * R_Node manager provides proxies for roadway nodes.
 *
 * @author Douglas Lau
 */
public class R_NodeManager extends ProxyManager<R_Node> {

	/** Name of "has GPS" style */
	static public final String STYLE_GPS = "Has GPS";

	/** Name of "no GPS" style */
	static public final String STYLE_NO_GPS = "No GPS";

	/** Name of "no location" style */
	static public final String STYLE_NO_LOC = "No Location";

	/** Set of all defined corridors */
	protected final TreeSet<String> corridors = new TreeSet<String>();

	/** List model of all corridors */
	protected final DefaultListModel model = new DefaultListModel();

	/** Get the corridor list model */
	public ListModel getCorridorModel() {
		return model;
	}

	/** TMS connection */
	protected final TmsConnection connection;

	/** Currently selected corridor */
	protected String corridor = null;

	/** Select a new freeway corridor */
	public void setCorridor(String c) {
		corridor = c;
	}

	/** Create a new roadway node manager */
	public R_NodeManager(TmsConnection tc, TypeCache<R_Node> c,
		GeoLocManager lm)
	{
		super(c, lm);
		connection = tc;
		initialize();
	}

	/** Add a new proxy to the r_node manager */
	protected void proxyAddedSlow(R_Node n) {
		super.proxyAddedSlow(n);
		String c = GeoLocHelper.getCorridor(n.getGeoLoc());
		if(c != null)
			addCorridor(c);
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
		else if(corridors.contains(s)) {
			String c = GeoLocHelper.getCorridor(getGeoLoc(proxy));
			return s.equals(c);
		} else
			return STYLE_ALL.equals(s);
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

	/** Add a corridor to the corridor model */
	protected void addCorridor(String cor) {
		if(corridors.add(cor)) {
			Iterator<String> it = corridors.iterator();
			for(int i = 0; it.hasNext(); i++) {
				String c = it.next();
				if(cor.equals(c)) {
					model.add(i, c);
					return;
				}
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
		return cache.find(new Checker<R_Node>() {
			public boolean check(R_Node n) {
				if(checkCorridor(n))
					return ch.check(n);
				else
					return false;
			}
		});
	}

	/** Check the corridor of an r_node */
	protected boolean checkCorridor(R_Node n) {
		String c = GeoLocHelper.getCorridor(n.getGeoLoc());
		return corridor == null || corridor.equals(c);
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
		SmartDesktop desktop = connection.getDesktop();
		try {
			desktop.show(new R_NodeProperties(connection, n));
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
		if(checkCorridor(proxy))
			return super.findGeoLoc(proxy);
		else
			return null;
	}

	/** Get the GeoLoc for the specified proxy */
	protected GeoLoc getGeoLoc(R_Node proxy) {
		return proxy.getGeoLoc();
	}
}
