/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2009  Minnesota Department of Transportation
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

import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.MapModel;

/**
 * Base class for all Iris tabs which contain maps
 *
 * @author Douglas Lau
 */
abstract public class MapTab extends IrisTab {

	/** Map model for the tab */
	protected final MapModel map_model = new MapModel();;

	/** Get the map model */
	public MapModel getMapModel() {
		return map_model;
	}

	/** Create a new map tab */
	public MapTab(String n, String t) {
		super(n, t);
	}

	/** Set the map */
	public void setMap(MapBean map) { }

	/** Perform any clean up necessary */
	public void dispose() {
		map_model.dispose();
	}
}
