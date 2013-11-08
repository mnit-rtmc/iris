/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
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
import java.awt.Component;
import java.util.LinkedList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.client.proxy.ProxyLayerState;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * The side panel contains the tab panel on the left side of the IRIS client.
 *
 * @author Douglas Lau
 */
public class SidePanel extends JPanel {

	/** Tabbed side pane */
	private final JTabbedPane tab_pane;

	/** Map associated with the pane */
	private final MapBean map;

	/** List of tab switchers */
	private final LinkedList<TabSwitcher> switchers =
		new LinkedList<TabSwitcher>();

	/** Create a new side panel */
	public SidePanel(MapBean m) {
		super(new BorderLayout());
		map = m;
		setMinimumSize(UI.dimension(500, 200));
		setPreferredSize(UI.dimension(500, 200));
		tab_pane = new JTabbedPane(JTabbedPane.TOP);
		add(tab_pane, BorderLayout.CENTER);
		tab_pane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				map.setPointSelector(null);
				setSelectedLayer(getSelectedHomeLayer());
			}
		});
	}

	/** Get the currently selected tab's home layer */
	private ProxyLayerState getSelectedHomeLayer() {
		Component tab = tab_pane.getSelectedComponent();
		if(tab instanceof MapTab)
			return getHomeProxyLayerState((MapTab)tab);
		else
			return null;
	}

	/** Get the home proxy layer state for a map tab */
	private ProxyLayerState getHomeProxyLayerState(MapTab mt) {
		LayerState ls = mt.getHomeLayer();
		if(ls instanceof ProxyLayerState)
			return (ProxyLayerState)ls;
		else
			return null;
	}

	/** Most recently selected layer */
	private ProxyLayerState sel_layer;

	/** Set the selected layer */
	private void setSelectedLayer(ProxyLayerState sel) {
		if(sel_layer != null && sel != sel_layer)
			sel_layer.setTabSelected(false);
		if(sel != null)
			sel.setTabSelected(true);
		sel_layer = sel;
		int ti = tab_pane.getSelectedIndex();
		if(ti >= 0)
			sel_tab = ti;
	}

	/** Last selected tab, which stores the index of last user selected
	 * tab. A field is used to track this, rather than dynamically
	 * calling tab_pane.getSelectedIndex() because if the method is
	 * called during app shutdown, it can erroneously return a -1. */
	private int sel_tab;

	/** Get the index of the currently selected tab */
	public int getSelectedTabIndex() {
		return sel_tab;
	}

	/** Set the currently selected tab */
	public void setSelectedTabIndex(int i) {
		if(i >= 0 && i < tab_pane.getTabCount())
			tab_pane.setSelectedIndex(i);
	}

	/** Add a tab to the screen pane */
	public void addTab(MapTab mt) {
		tab_pane.addTab(mt.getName(), null, mt, mt.getTip());
		mt.setMap(map);
		ProxyLayerState pls = getHomeProxyLayerState(mt);
		if(pls != null) {
			if(tab_pane.getTabCount() == 1)
				setSelectedLayer(pls);
			TabSwitcher ts = new TabSwitcher(mt,
				pls.getSelectionModel());
			switchers.add(ts);
		}
	}

	/** Class to listen for proxy selection events and select tabs */
	private class TabSwitcher implements ProxySelectionListener {
		private final MapTab tab;
		private final ProxySelectionModel model;
		protected TabSwitcher(MapTab mt, ProxySelectionModel psm) {
			tab = mt;
			model = psm;
			model.addProxySelectionListener(this);
		}
		protected void dispose() {
			model.removeProxySelectionListener(this);
		}
		@Override
		public void selectionAdded(SonarObject proxy) {
			tab_pane.setSelectedComponent(tab);
		}
		@Override
		public void selectionRemoved(SonarObject proxy) { }
	}

	/** Remove all the tabs */
	public void removeTabs() {
		for(TabSwitcher ts: switchers)
			ts.dispose();
		switchers.clear();
		tab_pane.removeAll();
	}

	/** Set the menu bar */
	public void setMenuBar(IMenuBar bar) {
		removeAll();
		if(bar != null)
			add(bar, BorderLayout.NORTH);
		add(tab_pane, BorderLayout.CENTER);
	}
}
