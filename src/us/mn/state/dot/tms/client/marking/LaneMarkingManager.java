/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.marking;

import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.DeviceManager;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * A lane marking manager is a container for SONAR lane marking objects.
 *
 * @author Douglas Lau
 */
public class LaneMarkingManager extends DeviceManager<LaneMarking> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<LaneMarking> descriptor(final Session s) {
		return new ProxyDescriptor<LaneMarking>(
			s.getSonarState().getLaneMarkings(), true
		) {
			@Override
			public LaneMarkingProperties createPropertiesForm(
				LaneMarking proxy)
			{
				return new LaneMarkingProperties(s, proxy);
			}
		};
	}

	/** Create a new lane marking manager */
	public LaneMarkingManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 0);
	}

	/** Create a theme for lane markings */
	@Override
	protected ProxyTheme<LaneMarking> createTheme() {
		ProxyTheme<LaneMarking> theme = new ProxyTheme<LaneMarking>(
			this, new LaneMarkingMarker());
		theme.addStyle(ItemStyle.NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(LaneMarking proxy) {
		return proxy.getGeoLoc();
	}
}
