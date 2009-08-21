/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.map.MapModel;
import us.mn.state.dot.tms.client.proxy.ProxyLayerState;
import us.mn.state.dot.tms.client.roads.SegmentLayerState;

/**
 * Super class of all tabs used in the IrisClient.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
abstract public class MapTab extends JPanel {

	/** Name of tab */
	protected final String name;

	/** Get the name of the tab */
	public String getName() {
		return name;
	}

	/** Get the tab number */
	abstract public int getNumber();

	/** Tip for hovering */
	protected final String tip;

	/** Get the tip */
	public String getTip() {
		return tip;
	}

	/** Map model for the tab */
	protected final MapModel map_model = new MapModel();

	/** Get the map model */
	public MapModel getMapModel() {
		return map_model;
	}

	/** Create a new map tab */
	public MapTab(String n, String t) {
		super(new BorderLayout());
		name = n;
		tip = t;
	}

	/** Set the map */
	public void setMap(MapBean map) {
		for(LayerState ls: map_model.getLayers()) {
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

	/** Perform any clean up necessary */
	public void dispose() {
		map_model.dispose();
	}
}
