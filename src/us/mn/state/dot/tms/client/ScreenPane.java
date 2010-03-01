/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.MapToolBar;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.MapExtent;
import us.mn.state.dot.tms.client.proxy.ProxyLayerState;
import us.mn.state.dot.tms.client.roads.SegmentLayerState;
import us.mn.state.dot.tms.client.toolbar.IrisToolBar;

/**
 * A screen pane is a pane which contains all components for one screen on
 * the IRIS client.
 *
 * @author Douglas Lau
 */
public class ScreenPane extends JPanel {

	/** Tabbed side pane */
	protected final JTabbedPane tab_pane;

	/** Map to be displayed on the screen pane */
	protected final MapBean map;

	/** Get the map */
	public MapBean getMap() {
		return map;
	}

	/** Map tool bar */
	protected final MapToolBar map_bar;

	/** Map panel */
	protected final JPanel map_panel;

	/** IRIS tool bar */
	protected final IrisToolBar tool_bar;

	/** Create a new screen pane */
	public ScreenPane() {
		setLayout(new BorderLayout());
		tab_pane = new JTabbedPane(SwingConstants.TOP);
		add(tab_pane, BorderLayout.WEST);
		map = new MapBean(true);
		map.setBackground(new Color(208, 216, 208));
		map_bar = createMapToolBar();
		tool_bar = new IrisToolBar(map);
		tool_bar.setFloatable(false);
		map_panel = createMapPanel();
		add(map_panel, BorderLayout.CENTER);
		tab_pane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Component tab = tab_pane.getSelectedComponent();
				if(tab instanceof MapTab) {
					MapTab mt = (MapTab)tab;
					map.getModel().setHomeLayer(
						mt.getHomeLayer(map));
				}
			}
		});
	}

	/** Set the map layers */
	public void setMapLayers() {
		for(LayerState ls: map.getLayers()) {
			if(ls instanceof ProxyLayerState) {
				ProxyLayerState pls = (ProxyLayerState)ls;
				pls.setMap(map);
			}
			if(ls instanceof SegmentLayerState) {
				SegmentLayerState sls = (SegmentLayerState)ls;
				sls.setMap(map);
			}
		}
	}

	/** Add a tab to the screen pane */
	public void addTab(MapTab tab) {
		tab_pane.addTab(tab.getName(), null, tab, tab.getTip());
	}

	/** Remove all the tabs */
	public void removeTabs() {
		tab_pane.removeAll();
		tool_bar.clear();
	}

	/** Create the map panel */
	protected JPanel createMapPanel() {
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

	/** Create a map tool bar with appropriate view buttons */
	protected MapToolBar createMapToolBar() {
		MapToolBar b = new MapToolBar(map);
		b.setFloatable(false);
		b.addHomeButton();
		return b;
	}

	/** Create the tool panels */
	public void createToolPanels(Session s) {
		TypeCache<MapExtent> c = s.getSonarState().getMapExtents();
		c.findObject(new Checker<MapExtent>() {
			public boolean check(MapExtent me) {
				map_bar.addButton(createMapButton(me));
				return false;
			}
		});
		tool_bar.createToolPanels(s);
	}

	/** Create a map extent button */
	protected JButton createMapButton(final MapExtent me) {
		JButton b = new JButton(me.getName());
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				map.zoomTo(new Rectangle2D.Double(
					me.getEasting(), me.getNorthing(),
					me.getEastSpan(), me.getNorthSpan()
				));
			}
		});
		return b;
	}
}
