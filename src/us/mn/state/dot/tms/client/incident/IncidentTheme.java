/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * Theme for incident objects on the map.
 *
 * @author Douglas Lau
 */
public class IncidentTheme extends ProxyTheme<Incident> {

	/** Incident Map object shape */
	static protected final Shape SHAPE = new IncidentMarker();

	/** Create a new incident theme */
	public IncidentTheme(IncidentManager man) {
		super(man, man.getProxyType(), SHAPE);
	}

	/** Draw a selected map object */
	public void drawSelected(Graphics2D g, MapObject mo, float scale) {
		if(mo instanceof MapGeoLoc) {
			MapGeoLoc loc = (MapGeoLoc)mo;
			loc.setShape(getShape(scale));
		}
		super.drawSelected(g, mo, scale);
	}

	/** Get the shape for a specified scale */
	protected Shape getShape(float scale) {
		return new IncidentMarker(32 * scale);
	}
}
