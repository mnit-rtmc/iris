/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import java.awt.Color;
import java.awt.Shape;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * An incident manager is a container for SONAR incident objects.
 *
 * @author Douglas Lau
 */
public class IncidentManager extends ProxyManager<Incident> {

	/** Incident Map object shape */
	static protected final Shape SHAPE = new IncidentMarker();

	/** Name of crash style */
	static public final String STYLE_CRASH = "Crash";

	/** Name of stall style */
	static public final String STYLE_STALL = "Stall";

	/** Name of blockage style */
	static public final String STYLE_BLOCKAGE = "Blockage";

	/** Name of construction style */
	static public final String STYLE_CONST = "Construction";

	/** Name of cleared style */
	static public final String STYLE_CLEARED = "Cleared";

	/** Name of all style */
	static public final String STYLE_ALL = "All";

	/** User session */
	protected final Session session;

	/** Create a new incident manager */
	public IncidentManager(Session s, TypeCache<Incident> c,
		GeoLocManager lm)
	{
		super(c, lm);
		session = s;
		cache.addProxyListener(this);
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "Incident";
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(Incident proxy, float scale) {
		return new IncidentMarker(32 * scale);
	}

	/** Create a styled theme for incidents */
	protected StyledTheme createTheme() {
		ProxyTheme<Incident> theme = new ProxyTheme<Incident>(this,
			getProxyType(), SHAPE);
		theme.addStyle(STYLE_CRASH, Color.RED);
		theme.addStyle(STYLE_STALL, Color.MAGENTA);
		theme.addStyle(STYLE_BLOCKAGE, Color.YELLOW);
		theme.addStyle(STYLE_CONST, Color.ORANGE);
		theme.addStyle(STYLE_CLEARED, Color.GREEN);
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		// There is no incident properties form
	}

	/** Create a popup menu for the selected proxy object(s) */
	protected JPopupMenu createPopup() {
		int n_selected = s_model.getSelectedCount();
		if(n_selected < 1)
			return null;
		if(n_selected == 1) {
			for(Incident inc: s_model.getSelected())
				return createSinglePopup(inc);
		}
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel("" + n_selected + " Incidents"));
		p.addSeparator();
		// FIXME: add menu item to clear incident
		return p;
	}

	/** Create a popup menu for a single incident selection */
	protected JPopupMenu createSinglePopup(Incident proxy) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		p.addSeparator();
		// FIXME: add menu item to clear incident
		return p;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(Incident proxy) {
		return new IncidentLoc(proxy);
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, Incident proxy) {
		EventType et = EventType.fromId(proxy.getEventType());
		if(STYLE_CRASH.equals(s))
			return et == EventType.INCIDENT_CRASH;
		else if(STYLE_STALL.equals(s))
			return et == EventType.INCIDENT_STALL;
		else if(STYLE_BLOCKAGE.equals(s))
			return et == EventType.INCIDENT_BLOCKAGE;
		else if(STYLE_CONST.equals(s))
			return et == EventType.INCIDENT_CONST;
		else if(STYLE_CLEARED.equals(s))
			return proxy.getCleared();
		else
			return STYLE_ALL.equals(s);
	}
}
