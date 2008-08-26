/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import java.awt.Color;
import javax.swing.JPopupMenu;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.client.sonar.GeoLocManager;
import us.mn.state.dot.tms.client.sonar.MapGeoLoc;
import us.mn.state.dot.tms.client.sonar.ProxyManager;
import us.mn.state.dot.tms.client.sonar.ProxyTheme;
import us.mn.state.dot.tms.client.sonar.StyleListModel;

/**
 * A camera manager is a container for SONAR camera objects.
 *
 * @author Douglas Lau
 */
public class CameraManager extends ProxyManager<Camera> {

	/** Name of active style */
	static protected final String STYLE_ACTIVE = "Active";

	/** Name of inactive style */
	static protected final String STYLE_INACTIVE = "Inactive";

	/** Name of unpublished style */
	static protected final String STYLE_UNPUBLISHED = "Not published";

	/** Name of list model containing all objects */
	static protected final String STYLE_ALL = "All";

	/** Color for active camera style */
	static protected final Color COLOR_ACTIVE = new Color(0, 192, 255);

	/** Create a new camera manager */
	public CameraManager(TypeCache<Camera> c, GeoLocManager lm) {
		super(c, lm);
		initialize();
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "Camera";
	}

	/** Create a styled theme for cameras */
	protected StyledTheme createTheme() {
		ProxyTheme<Camera> theme = new ProxyTheme<Camera>(this,
			getProxyType(), new CameraMarker());
		theme.addStyle(STYLE_UNPUBLISHED, ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(STYLE_INACTIVE, ProxyTheme.COLOR_INACTIVE,
			ProxyTheme.OUTLINE_INACTIVE);
		theme.addStyle(STYLE_ACTIVE, COLOR_ACTIVE);
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, Camera proxy) {
		if(STYLE_ACTIVE.equals(s)) {
			Controller ctr = proxy.getController();
			return ctr != null && ctr.getActive();
		} else
		if(STYLE_INACTIVE.equals(s)) {
			Controller ctr = proxy.getController();
			return ctr == null || !ctr.getActive();
		} else
		if(STYLE_UNPUBLISHED.equals(s))
			return !proxy.getPublish();
		else
			return STYLE_ALL.equals(s);
	}

	/** Get the style list model containing all proxies */
	protected StyleListModel<Camera> getAllModel() {
		return getStyleModel(STYLE_ALL);
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		// FIXME
	}

	/** Create a popup menu for the selected proxy object(s) */
	protected JPopupMenu createPopup() {
		JPopupMenu p = new JPopupMenu();
//		p.add(makeMenuLabel(id));
		p.add(new javax.swing.JLabel("Popup Test"));
		p.addSeparator();
		return p;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(Camera proxy) {
		return proxy.getGeoLoc();
	}
}
