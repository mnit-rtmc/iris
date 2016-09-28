/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import javax.swing.JPanel;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.client.map.LayerState;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;

/**
 * Side panel tab for main IRIS map interface.
 *
 * @author Douglas Lau
 */
abstract public class MapTab<T extends SonarObject> extends JPanel {

	/** Proxy manager */
	protected final ProxyManager<T> manager;

	/** Get the tab ID */
	public String getTabId() {
		return manager.getSonarType();
	}

	/** Name of side panel tab */
	private final String name;

	/** Get the name of the side panel tab */
	public String getName() {
		return name;
	}

	/** Tip for hovering */
	private final String tip;

	/** Get the tip */
	public String getTip() {
		return tip;
	}

	/** Listener to select side panel tab */
	private final ProxySelectionListener listener =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			if (side_pnl != null &&
			    getSelectionModel().getSelectedCount() > 0)
				side_pnl.setSelectedTab(MapTab.this);
		}
	};

	/** Create a new map tab */
	protected MapTab(ProxyManager<T> m) {
		super(new BorderLayout());
		manager = m;
		String t = m.getSonarType() + ".tab";
		name = I18N.get(t);
		tip = I18N.get(t + ".tooltip");
		getSelectionModel().addProxySelectionListener(listener);
	}

	/** Get the proxy selection model */
	private ProxySelectionModel<T> getSelectionModel() {
		return manager.getSelectionModel();
	}

	/** Initialize the map tab */
	abstract public void initialize();

	/** Perform any clean up necessary */
	public void dispose() {
		getSelectionModel().removeProxySelectionListener(listener);
		removeAll();
	}

	/** Side panel for this tab */
	private SidePanel side_pnl;

	/** Set the side panel */
	public void setSidePanel(SidePanel p) {
		side_pnl = p;
	}

	/** Set the screen pane */
	public void setScreenPane(ScreenPane sp) {
		manager.setScreenPane(sp);
	}

	/** Get the home layer for the tab */
	public LayerState getHomeLayer(MapBean m) {
		for (LayerState ls: m.getLayers()) {
			if (manager.checkLayer(ls.getLayer()))
				return ls;
		}
		return null;
	}

	/** Is this the selected tab? */
	public boolean isSelectedTab() {
		return side_pnl.getSelectedTab() == this;
	}
}
