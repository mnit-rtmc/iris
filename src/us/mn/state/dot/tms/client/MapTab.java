/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.map.Layer;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.client.proxy.ProxyManager;

/**
 * Side panel tab for main IRIS map interface.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
abstract public class MapTab<T extends SonarObject> extends JPanel {

	/** Proxy manager */
	protected final ProxyManager<T> manager;

	/** Map tab ID */
	private final String tab_id;

	/** Get the tab ID */
	public String getTabId() {
		return tab_id;
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

	/** Create a new map tab */
	protected MapTab(ProxyManager<T> m, String id) {
		super(new BorderLayout());
		tab_id = id;
		manager = m;
		name = I18N.get(tab_id);
		tip = I18N.get(tab_id + ".tab");
	}

	/** Create a new map tab */
	protected MapTab(ProxyManager<T> m) {
		this(m, m.getProxyType());
	}

	/** Perform any clean up necessary */
	public void dispose() {
		removeAll();
	}

	/** Current map for this tab */
	private MapBean map;

	/** Set the map for this tab */
	public void setMap(MapBean m) {
		map = m;
	}

	/** Get the home layer for the tab */
	public LayerState getHomeLayer(MapBean m) {
		for(LayerState ls: m.getLayers()) {
			if(isHomeLayer(ls.getLayer()))
				return ls;
		}
		return null;
	}

	/** Test if a layer is the home layer for the tab */
	private boolean isHomeLayer(Layer l) {
		return l == manager.getLayer();
	}
}
