/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import javax.swing.JPopupMenu;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * A lane marking manager is a container for SONAR lane marking objects.
 *
 * @author Douglas Lau
 */
public class LaneMarkingManager extends ProxyManager<LaneMarking> {

	/** Lane marking map object marker */
	static protected final LaneMarkingMarker MARKER =
		new LaneMarkingMarker();

	/** Name of deployed style */
	static public final String STYLE_DEPLOYED = "Deployed";

	/** Name of available style */
	static public final String STYLE_AVAILABLE = "Available";

	/** Name of failed style */
	static public final String STYLE_FAILED = "Failed";

	/** Name of "no controller" style */
	static public final String STYLE_NO_CONTROLLER = "No controller";

	/** User session */
	protected final Session session;

	/** Create a new lane marking manager */
	public LaneMarkingManager(Session s, TypeCache<LaneMarking> c,
		GeoLocManager lm)
	{
		super(c, lm);
		session = s;
		cache.addProxyListener(this);
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "Lane Marking";
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(AffineTransform at) {
		return MARKER.createTransformedShape(at);
	}

	/** Create a styled theme for lane markings */
	protected StyledTheme createTheme() {
		ProxyTheme<LaneMarking> theme = new ProxyTheme<LaneMarking>(
			this, getProxyType(), MARKER);
		theme.addStyle(STYLE_NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, LaneMarking proxy) {
		if(STYLE_NO_CONTROLLER.equals(s))
			return proxy.getController() == null;
		else
			return STYLE_ALL.equals(s);
	}

	/** Test if a controller is failed */
	static protected boolean isControllerFailed(Controller ctr) {
		return ctr == null || !("".equals(ctr.getStatus()));
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		// FIXME
	}

	/** Create a popup menu for the selected proxy object(s) */
	protected JPopupMenu createPopup() {
		// No popup
		return null;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(LaneMarking proxy) {
		return proxy.getGeoLoc();
	}
}
