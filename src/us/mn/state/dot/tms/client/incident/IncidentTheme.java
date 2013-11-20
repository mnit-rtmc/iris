/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.Style;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * Theme for incident objects on the map.
 *
 * @author Douglas Lau
 */
public class IncidentTheme extends ProxyTheme<Incident> {

	/** Incident Map object marker */
	static private final IncidentMarker MARKER = new IncidentMarker();

	/** Create a new incident theme */
	public IncidentTheme(IncidentManager man) {
		super(man, MARKER);
	}

	/** Get an appropriate style for the given map object */
	public Style getStyle(MapObject mo) {
		if(mo instanceof IncidentGeoLoc) {
			IncidentGeoLoc loc = (IncidentGeoLoc)mo;
			return getStyle(loc.getIncident());
		}
		return dstyle;
	}

	/** Draw a map object */
	@Override
	public void draw(Graphics2D g, MapObject mo, float scale) {
		manager.setShape(getShape(scale));
		super.draw(g, mo, scale);
	}

	/** Draw a selected map object */
	@Override
	public void drawSelected(Graphics2D g, MapObject mo, float scale) {
		manager.setShape(getShape(scale));
		super.drawSelected(g, mo, scale);
	}

	/** Get the shape for a specified scale */
	private Shape getShape(float scale) {
		float sc = ProxyManager.adjustScale(scale);
		AffineTransform at = new AffineTransform();
		at.setToScale(sc, sc);
		return MARKER.createTransformedShape(at);
	}
}
