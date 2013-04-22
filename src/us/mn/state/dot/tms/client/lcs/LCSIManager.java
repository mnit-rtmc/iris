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
package us.mn.state.dot.tms.client.lcs;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import javax.swing.JPopupMenu;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * The LCSIManager class provides proxies for LCSIndication objects.
 *
 * @author Douglas Lau
 */
public class LCSIManager extends ProxyManager<LCSIndication> {

	/** Get the LCS indication cache */
	static protected TypeCache<LCSIndication> getCache(Session s) {
		LcsCache cache = s.getSonarState().getLcsCache();
		return cache.getLCSIndications();
	}

	/** Create a new LCS indicaiton manager */
	public LCSIManager(Session s, GeoLocManager lm) {
		super(getCache(s), lm);
		cache.addProxyListener(this);
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "LCSIndication";
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(AffineTransform at) {
		return null;
	}

	/** Create a theme for LCS arrays */
	protected ProxyTheme<LCSIndication> createTheme() {
		ProxyTheme<LCSIndication> theme = new ProxyTheme<LCSIndication>(
			this, new LcsMarker());
		theme.addStyle(ItemStyle.NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(ItemStyle is, LCSIndication proxy) {
		switch(is) {
		case NO_CONTROLLER:
			return proxy.getController() == null;
		case ALL:
			return true;
		default:
			return false;
		}
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		// No properties form
	}

	/** Create a popup menu for the selected proxy object(s) */
	protected JPopupMenu createPopup() {
		// No popup
		return null;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(LCSIndication proxy) {
		return null;
	}

	/** Get the layer zoom visibility threshold */
	protected int getZoomThreshold() {
		return 19;
	}
}
