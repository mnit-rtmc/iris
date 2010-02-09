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
package us.mn.state.dot.tms.client.incident;

import java.awt.Color;
import java.awt.Shape;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.roads.R_NodeManager;

/**
 * An incident manager is a container for SONAR incident objects.
 *
 * @author Douglas Lau
 */
public class IncidentManager extends ProxyManager<Incident> {

	/** Name of crash style */
	static public final String STYLE_CRASH = "Crash";

	/** Name of stall style */
	static public final String STYLE_STALL = "Stall";

	/** Name of debris style */
	static public final String STYLE_DEBRIS = "Debris";

	/** Name of road work style */
	static public final String STYLE_ROADWORK = "Road Work";

	/** Name of cleared style */
	static public final String STYLE_CLEARED = "Cleared";

	/** Name of all style */
	static public final String STYLE_ALL = "All";

	/** User session */
	protected final Session session;

	/** R_Node manager */
	protected final R_NodeManager r_node_manager;

	/** Location mapping */
	protected final HashMap<String, IncidentGeoLoc> locations =
		new HashMap<String, IncidentGeoLoc>();

	/** Create a new incident manager */
	public IncidentManager(Session s, TypeCache<Incident> c,
		GeoLocManager lm, R_NodeManager r_man)
	{
		super(c, lm);
		session = s;
		r_node_manager = r_man;
		cache.addProxyListener(this);
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "Incident";
	}

	/** Create a list cell renderer */
	public ListCellRenderer createCellRenderer() {
		return new IncidentCellRenderer(this);
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(Incident proxy, float scale) {
		return new IncidentMarker(scale);
	}

	/** Create a styled theme for incidents */
	protected StyledTheme createTheme() {
		IncidentTheme theme = new IncidentTheme(this);
		theme.addStyle(STYLE_CLEARED, new Color(128, 255, 128));
		theme.addStyle(STYLE_CRASH, new Color(255, 128, 128));
		theme.addStyle(STYLE_STALL, new Color(255, 128, 255));
		theme.addStyle(STYLE_DEBRIS, new Color(255, 255, 128));
		theme.addStyle(STYLE_ROADWORK, new Color(255, 208, 128));
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
	public MapGeoLoc findGeoLoc(Incident proxy) {
		String name = proxy.getName();
		if(locations.containsKey(name))
			return locations.get(name);
		IncidentGeoLoc loc = new IncidentGeoLoc(proxy,
			getGeoLoc(proxy));
		loc_manager.setTangentAngle(loc);
		locations.put(name, loc);
		return loc;
	}

	/** Remove a map geo location for an incident */
	public void removeIncident(String name) {
		locations.remove(name);
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(Incident proxy) {
		IncidentLoc loc = new IncidentLoc(proxy);
		CorridorBase cb = r_node_manager.lookupCorridor(loc);
		if(cb != null) {
			R_Node rnd = cb.findNearest(loc);
			if(rnd != null)
				return new IncidentLoc(proxy, rnd.getGeoLoc());
		}
		return loc;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, Incident proxy) {
		EventType et = EventType.fromId(proxy.getEventType());
		if(STYLE_CRASH.equals(s))
			return et == EventType.INCIDENT_CRASH;
		else if(STYLE_STALL.equals(s))
			return et == EventType.INCIDENT_STALL;
		else if(STYLE_DEBRIS.equals(s))
			return et == EventType.INCIDENT_DEBRIS;
		else if(STYLE_ROADWORK.equals(s))
			return et == EventType.INCIDENT_ROADWORK;
		else if(STYLE_CLEARED.equals(s))
			return proxy.getCleared();
		else
			return STYLE_ALL.equals(s);
	}

	/** Get the style for an event type */
	public String getStyle(EventType et) {
		switch(et) {
		case INCIDENT_CRASH:
			return STYLE_CRASH;
		case INCIDENT_STALL:
			return STYLE_STALL;
		case INCIDENT_DEBRIS:
			return STYLE_DEBRIS;
		case INCIDENT_ROADWORK:
			return STYLE_ROADWORK;
		default:
			return null;
		}
	}

	/** Get the lane type description */
	protected String getLaneType(LaneType lt) {
		switch(lt) {
		case MAINLINE:
		case EXIT:
		case MERGE:
		case CD_LANE:
			return lt.toString();
		default:
			return null;
		}
	}

	/** Get the event description */
	protected String getDesc(String sty, String ltd) {
		if(ltd != null)
			return sty + " on " + ltd;
		else
			return sty;
	}

	/** Update the event type on a label */
	public void setTypeLabel(Incident inc, JLabel lbl) {
		EventType et = EventType.fromId(inc.getEventType());
		String sty = getStyle(et);
		LaneType lt = LaneType.fromOrdinal(inc.getLaneType());
		String ltd = getLaneType(lt);
		if(sty != null) {
			lbl.setText(getDesc(sty, ltd));
			Symbol sym = getTheme().getSymbol(sty);
			if(sym != null)
				lbl.setIcon(sym.getLegend());
			else
				lbl.setIcon(null);
		} else {
			lbl.setText("");
			lbl.setIcon(null);
		}
	}
}
