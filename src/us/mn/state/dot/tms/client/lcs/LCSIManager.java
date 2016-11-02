/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * The LCSIManager class provides proxies for LCSIndication objects.
 *
 * @author Douglas Lau
 */
public class LCSIManager extends ProxyManager<LCSIndication> {

	/** Create a proxy descriptor */
	static private ProxyDescriptor<LCSIndication> descriptor(Session s) {
		return new ProxyDescriptor<LCSIndication>(
			s.getSonarState().getLcsCache().getLCSIndications(),
			false
		);
	}

	/** Create a new LCS indicaiton manager */
	public LCSIManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 0);
	}

	/** Create a theme for LCS arrays */
	@Override
	protected ProxyTheme<LCSIndication> createTheme() {
		ProxyTheme<LCSIndication> theme = new ProxyTheme<LCSIndication>(
			this, new LcsMarker());
		theme.addStyle(ItemStyle.NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, LCSIndication proxy) {
		switch (is) {
		case NO_CONTROLLER:
			return proxy.getController() == null;
		case ALL:
			return true;
		default:
			return false;
		}
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(LCSIndication proxy) {
		return null;
	}
}
