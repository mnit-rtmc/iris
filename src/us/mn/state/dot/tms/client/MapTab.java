/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Side panel tab for main IRIS map interface.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
abstract public class MapTab extends JPanel {

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

	/** Current map for this tab */
	private MapBean map;

	/** Set the map for this tab */
	public void setMap(MapBean m) {
		map = m;
	}

	/** Get the home layer for the tab */
	public LayerState getHomeLayer() {
		MapBean m = map;
		if(m != null) {
			for(LayerState ls: m.getLayers()) {
				String ln = ls.getLayer().getName();
				if(ln.equals(name))
					return ls;
			}
		}
		return null;
	}

	/** Create a new map tab */
	public MapTab(String text_id) {
		super(new BorderLayout());
		name = I18N.get(text_id);
		tip = I18N.get(text_id + ".tab");
	}

	/** Perform any clean up necessary */
	public void dispose() {
		removeAll();
	}
}
