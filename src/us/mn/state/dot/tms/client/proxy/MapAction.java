/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.client.IrisClient;
import us.mn.state.dot.tms.geo.ZoomLevel;

/**
 * An action to center the map on a proxy object.
 *
 * @author Douglas Lau
 */
public class MapAction<T extends SonarObject> extends ProxyAction<T> {

	/** IRIS client */
	private final IrisClient client;

	/** Latitude */
	private final Double lat;

	/** Longitude */
	private final Double lon;

	/** Create a new map action */
	public MapAction(IrisClient ic, T p, GeoLoc gl) {
		this(ic, p, gl != null ? gl.getLat() : null,
		            gl != null ? gl.getLon() : null);
	}

	/** Create a new map action */
	public MapAction(IrisClient ic, T p, Double lat, Double lon) {
		super("location.map.center", p);
		client = ic;
		this.lat = lat;
		this.lon = lon;
	}

	/** Actually perform the action */
	@Override
	protected void doActionPerformed(ActionEvent e) {
		if (lat != null && lon != null) {
			// FIXME: only set extent on current ScreenPane
			client.setMapExtent(ZoomLevel.FIFTEEN, lat.floatValue(),
				lon.floatValue());
		}
	}
}
