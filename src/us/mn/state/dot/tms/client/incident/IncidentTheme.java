/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2015  Minnesota Department of Transportation
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

import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.Style;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * Theme for incident objects on the map.
 *
 * @author Douglas Lau
 */
public class IncidentTheme extends ProxyTheme<Incident> {

	/** Create a new incident theme */
	public IncidentTheme(IncidentManager man) {
		super(man, new IncidentMarker());
	}

	/** Get an appropriate style for the given map object */
	@Override
	public Style getStyle(MapObject mo) {
		if (mo instanceof IncidentGeoLoc) {
			IncidentGeoLoc loc = (IncidentGeoLoc)mo;
			return getStyle(loc.getIncident());
		}
		return dstyle;
	}
}
