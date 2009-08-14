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
package us.mn.state.dot.tms.client.lcs;

import java.awt.Shape;
import javax.swing.JPopupMenu;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GeoLoc;
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

	/** Name of "no controller" style */
	static public final String STYLE_NO_CONTROLLER = "No controller";

	/** Name of all style */
	static public final String STYLE_ALL = "All";

	/** Get the LCS indication cache */
	static protected TypeCache<LCSIndication> getCache(Session s) {
		LcsCache cache = s.getSonarState().getLcsCache();
		return cache.getLCSIndications();
	}

	/** Create a new LCS indicaiton manager */
	public LCSIManager(Session s, GeoLocManager lm) {
		super(getCache(s), lm);
		initialize();
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "LCSIndication";
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(LCSIndication proxy) {
		return null;
	}

	/** Create a styled theme for LCS arrays */
	protected StyledTheme createTheme() {
		ProxyTheme<LCSIndication> theme = new ProxyTheme<LCSIndication>(
			this, getProxyType(), new LcsMarker());
		theme.addStyle(STYLE_NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, LCSIndication proxy) {
		if(STYLE_NO_CONTROLLER.equals(s))
			return proxy.getController() == null;
		else
			return STYLE_ALL.equals(s);
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
}
