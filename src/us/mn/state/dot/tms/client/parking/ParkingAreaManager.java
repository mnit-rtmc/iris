/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.parking;

import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.ParkingArea;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * A parking are manager is a container for SONAR parking area objects.
 *
 * @author Douglas Lau
 */
public class ParkingAreaManager extends ProxyManager<ParkingArea> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<ParkingArea> descriptor(final Session s) {
		return new ProxyDescriptor<ParkingArea>(
			s.getSonarState().getParkingAreas(),
			true,	/* has_properties */
			true,	/* has_create_delete */
			false	/* has_name */
		) {
			@Override
			public ParkingAreaProperties createPropertiesForm(
				ParkingArea proxy)
			{
				return new ParkingAreaProperties(s, proxy);
			}
			@Override
			public ParkingAreaForm makeTableForm() {
				return new ParkingAreaForm(s);
			}
		};
	}

	/** Create a new parking area manager */
	public ParkingAreaManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 17);
	}

	/** Create a theme for parking areas */
	@Override
	protected ProxyTheme<ParkingArea> createTheme() {
		ProxyTheme<ParkingArea> theme = new ProxyTheme<ParkingArea>(
			this, new ParkingAreaMarker());
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, ParkingArea proxy) {
		return true;
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(ParkingArea proxy) {
		return proxy.getGeoLoc();
	}

	/** Create a parking area map tab */
	@Override
	public ParkingAreaTab createTab() {
		return new ParkingAreaTab(session, this);
	}
}
