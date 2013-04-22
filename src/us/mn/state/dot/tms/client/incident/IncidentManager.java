/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2013  Minnesota Department of Transportation
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
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.StyleSummary;
import us.mn.state.dot.tms.utils.I18N;

/**
 * An incident manager is a container for SONAR incident objects.
 *
 * @author Douglas Lau
 */
public class IncidentManager extends ProxyManager<Incident> {

	/** Incident Map object marker */
	static protected final IncidentMarker MARKER = new IncidentMarker();

	/** Get the incident cache */
	static protected TypeCache<Incident> getCache(Session s) {
		return s.getSonarState().getIncidents();
	}

	/** User session */
	protected final Session session;

	/** Location mapping */
	protected final HashMap<String, IncidentGeoLoc> locations =
		new HashMap<String, IncidentGeoLoc>();

	/** Create a new incident manager */
	public IncidentManager(Session s, GeoLocManager lm) {
		super(getCache(s), lm);
		session = s;
		cache.addProxyListener(this);
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return I18N.get("incident");
	}

	/** Create a list cell renderer */
	public ListCellRenderer createCellRenderer() {
		return new IncidentCellRenderer(this);
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(AffineTransform at) {
		return MARKER.createTransformedShape(at);
	}

	/** Create a theme for incidents */
	protected IncidentTheme createTheme() {
		IncidentTheme theme = new IncidentTheme(this);
		theme.addStyle(ItemStyle.CLEARED, new Color(128, 255, 128));
		theme.addStyle(ItemStyle.CRASH, new Color(255, 128, 128));
		theme.addStyle(ItemStyle.STALL, new Color(255, 128, 255));
		theme.addStyle(ItemStyle.ROADWORK, new Color(255, 208, 128));
		theme.addStyle(ItemStyle.HAZARD, new Color(255, 255, 128));
		theme.addStyle(ItemStyle.ALL);
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
		p.add(new JLabel(I18N.get("incident.plural") + ": " +
			n_selected));
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
		Double tan = loc_manager.getTangentAngle(loc);
		if(tan != null)
			loc.setTangent(tan);
		locations.put(name, loc);
		return loc;
	}

	/** Remove a map geo location for an incident */
	public void removeIncident(String name) {
		locations.remove(name);
	}

	/** Find the map geo location for a proxy */
	protected IncidentLoc getGeoLoc(Incident proxy) {
		IncidentLoc loc = new IncidentLoc(proxy);
		CorridorBase cb = lookupCorridor(loc);
		if(cb != null) {
			R_Node rnd = cb.findNearest(loc);
			if(rnd != null)
				return new IncidentLoc(proxy, rnd.getGeoLoc());
		}
		return loc;
	}

	/** Lookup the corridor for an incident location */
	public CorridorBase lookupCorridor(IncidentLoc loc) {
		return session.getR_NodeManager().lookupCorridor(loc);
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(ItemStyle is, Incident proxy) {
		// FIXME: this should not be necessary, but we're getting
		//        NullPointerExceptions here...
		if(proxy == null)
			return false;
		Integer iet = proxy.getEventType();
		if(iet == null)
			return false;
		EventType et = EventType.fromId(iet);
		switch(is) {
		case CRASH:
			return et == EventType.INCIDENT_CRASH;
		case STALL:
			return et == EventType.INCIDENT_STALL;
		case ROADWORK:
			return et == EventType.INCIDENT_ROADWORK;
		case HAZARD:
			return et == EventType.INCIDENT_HAZARD;
		case CLEARED:
			return proxy.getCleared();
		case ALL:
			return true;
		default:
			return false;
		}
	}

	/** Get the style for an event type */
	public String getStyle(EventType et) {
		switch(et) {
		case INCIDENT_CRASH:
			return ItemStyle.CRASH.toString();
		case INCIDENT_STALL:
			return ItemStyle.STALL.toString();
		case INCIDENT_ROADWORK:
			return ItemStyle.ROADWORK.toString();
		case INCIDENT_HAZARD:
			return ItemStyle.HAZARD.toString();
		default:
			return null;
		}
	}

	/** Get the description of an incident */
	public String getDescription(Incident inc) {
		String td = getTypeDesc(inc);
		if(td.length() > 0) {
			String loc = getGeoLoc(inc).getDescription();
			return td + " -- " + loc;
		} else
			return "";
	}

	/** Get the incident type description */
	public String getTypeDesc(Incident inc) {
		EventType et = EventType.fromId(inc.getEventType());
		String sty = getStyle(et);
		if(sty != null) {
			LaneType lt = LaneType.fromOrdinal(inc.getLaneType());
			return getTypeDesc(sty, getLaneType(lt));
		} else
			return "";
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

	/** Get the incident type description.
	 * @param sty Style of incident.
	 * @param ltd Lane type description (may be null).
	 * @return Description of incident type. */
	protected String getTypeDesc(String sty, String ltd) {
		if(ltd != null)
			return sty + " " + I18N.get("incident.on") + " " + ltd;
		else
			return sty;
	}

	/** Get the symbol for an incident */
	public Symbol getSymbol(Incident inc) {
		EventType et = EventType.fromId(inc.getEventType());
		String sty = getStyle(et);
		if(sty != null)
			return getTheme().getSymbol(sty);
		else
			return null;
	}

	/** Get the icon for an incident */
	public Icon getIcon(Incident inc) {
		if(inc != null) {
			Symbol sym = getSymbol(inc);
			if(sym != null)
				return sym.getLegend();
		}
		String st = ItemStyle.CLEARED.toString();
		return getTheme().getSymbol(st).getLegend();
	}

	/** Get the layer zoom visibility threshold */
	protected int getZoomThreshold() {
		return 10;
	}
}
