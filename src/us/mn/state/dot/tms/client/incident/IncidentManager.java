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
package us.mn.state.dot.tms.client.incident;

import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.geokit.Position;
import us.mn.state.dot.map.Style;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.LaneConfiguration;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapAction;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * An incident manager is a container for SONAR incident objects.
 *
 * @author Douglas Lau
 */
public class IncidentManager extends ProxyManager<Incident> {

	/** Location mapping */
	private final HashMap<String, IncidentGeoLoc> locations =
		new HashMap<String, IncidentGeoLoc>();

	/** Create a new incident manager */
	public IncidentManager(Session s, GeoLocManager lm) {
		super(s, lm);
	}

	/** Get the sonar type name */
	@Override
	public String getSonarType() {
		return Incident.SONAR_TYPE;
	}

	/** Get the incident cache */
	@Override
	public TypeCache<Incident> getCache() {
		return session.getSonarState().getIncCache().getIncidents();
	}

	/** Create an incident map tab */
	@Override
	public IncidentTab createTab() {
		return new IncidentTab(session, this);
	}

	/** Create a list cell renderer */
	@Override
	public ListCellRenderer<Incident> createCellRenderer() {
		return new IncidentCellRenderer(this);
	}

	/** Create a theme for incidents */
	@Override
	protected IncidentTheme createTheme() {
		return new IncidentTheme(this);
	}

	/** Create a popup menu for a single incident selection */
	@Override
	protected JPopupMenu createPopupSingle(Incident proxy) {
		SmartDesktop desktop = session.getDesktop();
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		p.addSeparator();
		p.add(new MapAction<Incident>(desktop.client, proxy,
			proxy.getLat(), proxy.getLon()));
		p.addSeparator();
		// FIXME: add menu item to clear incident
		return p;
	}

	/** Create a popup menu for multiple objects */
	@Override
	protected JPopupMenu createPopupMulti(int n_selected) {
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel(I18N.get("incident.title") + ": " +
			n_selected));
		p.addSeparator();
		// FIXME: add menu item to clear incident
		return p;
	}

	/** Find the map geo location for a proxy */
	@Override
	public MapGeoLoc findGeoLoc(Incident proxy) {
		String name = proxy.getName();
		if (locations.containsKey(name))
			return locations.get(name);
		IncidentGeoLoc loc = new IncidentGeoLoc(proxy,
			getGeoLoc(proxy));
		loc.setManager(this);
		loc.doUpdate();
		locations.put(name, loc);
		return loc;
	}

	/** Remove a map geo location for a client incident */
	public void removeClientIncident() {
		locations.remove(ClientIncident.NAME);
	}

	/** Find the map geo location for a proxy */
	@Override
	protected IncidentLoc getGeoLoc(Incident proxy) {
		IncidentLoc loc = new IncidentLoc(proxy);
		CorridorBase cb = lookupCorridor(loc);
		if (cb != null) {
			R_Node rnd = cb.findNearest(loc);
			if (rnd != null)
				return new IncidentLoc(proxy, rnd.getGeoLoc());
		}
		return loc;
	}

	/** Lookup the corridor for an incident location */
	public CorridorBase lookupCorridor(IncidentLoc loc) {
		return session.getR_NodeManager().lookupCorridor(loc);
	}

	/** Get lane configuration at an incident */
	public LaneConfiguration laneConfiguration(Incident inc) {
		LaneType lt = LaneType.fromOrdinal(inc.getLaneType());
		if (lt.isRamp())
			return rampLaneConfiguration(inc);
		IncidentLoc loc = new IncidentLoc(inc);
		CorridorBase cb = lookupCorridor(loc);
		if (cb != null)
			return cb.laneConfiguration(getWgs84Position(inc));
		else
			return new LaneConfiguration(0, 0);
	}

	/** Get ramp lane configuration at an incident */
	private LaneConfiguration rampLaneConfiguration(Incident inc) {
		int lanes = inc.getImpact().length();
		int left = 5 - lanes / 2;
		int right = left + lanes - 2;
		return new LaneConfiguration(left, right);
	}

	/** Get Position in WGS84 */
	private Position getWgs84Position(Incident inc) {
		return new Position(inc.getLat(), inc.getLon());
	}

	/** Check if a given attribute affects a proxy style */
	@Override
	public boolean isStyleAttrib(String a) {
		return "cleared".equals(a) || "confirmed".equals(a);
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, Incident proxy) {
		EventType et = getEventType(proxy);
		if (et == null)
			return false;
		switch (is) {
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
		case UNCONFIRMED:
			return !proxy.getConfirmed();
		case ALL:
			return true;
		default:
			return false;
		}
	}

	/** Get the event type of an incident */
	static private EventType getEventType(Incident proxy) {
		try {
			Integer iet = proxy.getEventType();
			return iet != null ? EventType.fromId(iet) : null;
		}
		catch (NullPointerException e) {
			// FIXME: there is a sonar bug which throws NPE when
			//        an incident proxy object is deleted
			return null;
		}
	}

	/** Get the description of an incident */
	@Override
	public String getDescription(Incident inc) {
		String td = getTypeDesc(inc);
		if (td.length() > 0) {
			String loc = getGeoLoc(inc).getDescription();
			return td + " -- " + loc;
		} else
			return "";
	}

	/** Get the incident type description */
	public String getTypeDesc(Incident inc) {
		Style sty = getTheme().getStyle(inc);
		if (sty != null) {
			LaneType lt = LaneType.fromOrdinal(inc.getLaneType());
			return getTypeDesc(sty.toString(), getLaneType(lt));
		} else
			return "";
	}

	/** Get the lane type description */
	private String getLaneType(LaneType lt) {
		switch (lt) {
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
	private String getTypeDesc(String sty, String ltd) {
		if (ltd != null)
			return sty + " " + I18N.get("incident.on") + " " + ltd;
		else
			return sty;
	}

	/** Get the style for an incident */
	public Style getStyle(Incident inc) {
		return getTheme().getStyle(inc);
	}

	/** Get the icon for an incident */
	public Icon getIcon(Incident inc) {
		if (inc != null) {
			Style sty = getStyle(inc);
			if (sty != null)
				return getTheme().getLegend(sty);
		}
		Style sty = getTheme().getStyle(ItemStyle.CLEARED.toString());
		return getTheme().getLegend(sty);
	}

	/** Get the layer zoom visibility threshold */
	@Override
	protected int getZoomThreshold() {
		return 10;
	}
}
