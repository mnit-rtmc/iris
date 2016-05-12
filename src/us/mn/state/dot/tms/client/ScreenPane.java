/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
 * Copyright (C) 2010  AHMCT, University of California
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
package us.mn.state.dot.tms.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.MapExtent;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.map.MapToolBar;
import us.mn.state.dot.tms.client.toolbar.IrisToolBar;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;
import us.mn.state.dot.tms.geo.ZoomLevel;

/**
 * A screen pane is a pane which contains all components for one screen on
 * the IRIS client.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class ScreenPane extends JPanel {

	/** Create a point in spherical mercator units.
	 * @param lat Latitude coordinate.
	 * @param lon Longitude coordinate.
	 * @return Point in spherical mercator units. */
	static private Point2D createPoint(float lat, float lon) {
		SphericalMercatorPosition c = SphericalMercatorPosition.convert(
			new Position(lat, lon));
		return new Point2D.Double(c.getX(), c.getY());
	}

	/** Map to be displayed on the screen pane */
	private final MapBean map;

	/** Get the map */
	public MapBean getMap() {
		return map;
	}

	/** Side panel for tabs and menu */
	private final SidePanel side_panel;

	/** Map tool bar */
	private final MapToolBar map_bar;

	/** Map panel */
	private final JPanel map_panel;

	/** IRIS tool bar */
	private final IrisToolBar tool_bar;

	/** Create a new screen pane */
	public ScreenPane() {
		setLayout(new BorderLayout());
		map = new MapBean(true);
		map.setBackground(new Color(208, 216, 208));
		side_panel = new SidePanel(map);
		add(side_panel, BorderLayout.WEST);
		map_bar = new MapToolBar(map);
		map_bar.setFloatable(false);
		tool_bar = new IrisToolBar(map);
		tool_bar.setFloatable(false);
		map_panel = createMapPanel();
		add(map_panel, BorderLayout.CENTER);
	}

	/** Get the ID of the currently selected tab */
	public String getSelectedTabId() {
		return side_panel.getSelectedTabId();
	}

	/** Set the currently selected tab */
	public void setSelectedTab(MapTab mt) {
		side_panel.setSelectedTab(mt);
	}

	/** Add a tab to the screen pane */
	public void addTab(MapTab mt) {
		side_panel.addTab(mt);
	}

	/** Remove all the tabs */
	public void removeTabs() {
		side_panel.removeTabs();
	}

	/** Set the menu bar */
	public void setMenuBar(IMenuBar bar) {
		side_panel.setMenuBar(bar);
	}

	/** Create the map panel */
	private JPanel createMapPanel() {
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createBevelBorder(
			BevelBorder.LOWERED));
		p.add(map, BorderLayout.CENTER);
		JPanel mp = new JPanel(new BorderLayout());
		mp.add(map_bar, BorderLayout.NORTH);
		mp.add(p, BorderLayout.CENTER);
		mp.add(tool_bar, BorderLayout.SOUTH);
		return mp;
	}

	/** Create the tool panels */
	public void createToolPanels(Session s) {
		map_bar.addMenu();
		TreeMap<String, MapExtent> extents = buildExtents(s);
		for (String n: extents.keySet()) {
			MapExtent me = extents.get(n);
			map_bar.addButton(createMapButton(me));
		}
		tool_bar.createToolPanels(s);
	}

	/** Build a mapping of extent names to map extents */
	private TreeMap<String, MapExtent> buildExtents(Session s) {
		TypeCache<MapExtent> tc = s.getSonarState().getMapExtents();
		TreeMap<String, MapExtent> extents =
			new TreeMap<String, MapExtent>();
		for (MapExtent me: tc)
			extents.put(me.getName(), me);
		return extents;
	}

	/** Create a map extent button */
	private JButton createMapButton(final MapExtent me) {
		JButton b = new JButton(me.getName());
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				setMapExtent(me);
			}
		});
		return b;
	}

	/** Set the map extent */
	public void setMapExtent(MapExtent me) {
		ZoomLevel zoom = ZoomLevel.fromOrdinal(me.getZoom());
		if (zoom != null)
			setMapExtent(zoom, me.getLat(), me.getLon());
	}

	/** Set the map extent.
	 * @param zoom Zoom level.
	 * @param lat Latitude coordinate.
	 * @param lon Longitude coordinate. */
	public void setMapExtent(ZoomLevel zoom, float lat, float lon) {
		Point2D ctr = createPoint(lat, lon);
		map.getModel().setExtent(ctr, zoom);
	}

	/** Clear the tool panels */
	public void clearToolPanels() {
		map_bar.clear();
		tool_bar.clear();
	}
}
