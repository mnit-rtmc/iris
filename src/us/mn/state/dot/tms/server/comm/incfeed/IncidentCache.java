/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.incfeed;

import java.util.Date;
import java.util.HashSet;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentDetail;
import us.mn.state.dot.tms.IncidentHelper;
import us.mn.state.dot.tms.IncidentImpact;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;
import static us.mn.state.dot.tms.server.BaseObjectImpl.corridors;
import us.mn.state.dot.tms.server.IncidentImpl;

/**
 * Cache of incidents in an incident feed.
 *
 * @author Douglas Lau
 */
public class IncidentCache {

	/** Create an incident and notify clients.
	 * @param n Incident name.
	 * @param pi Parsed incident.
	 * @param loc Geo location.
	 * @param im Lane impact. */
	static private boolean createIncidentNotify(String n, ParsedIncident pi,
		GeoLoc loc, String im)
	{
		short lnt = (short) LaneType.MAINLINE.ordinal();
		IncidentImpl inc = new IncidentImpl(n, null,
			pi.inc_type.ordinal(), new Date(), pi.detail,
			lnt, loc.getRoadway(), loc.getRoadDir(),
			pi.lat, pi.lon, pi.lookupCamera(), im, false, false);
		try {
			inc.notifyCreate();
			return true;
		}
		catch (SonarException e) {
			// Probably a cleared incident with the same name
			System.err.println("createNotify: " + e.getMessage());
			return false;
		}
	}

	/** Comm link name */
	private final String link;

	/** Incident feed debug log */
	private final DebugLog inc_log;

	/** Set of next incidents */
	private final HashSet<String> nxt = new HashSet<String>();

	/** Set of active incidents */
	private final HashSet<String> incidents = new HashSet<String>();

	/** Flag to incidate cache has been updated */
	private boolean updated = false;

	/** Create a new incident cache */
	public IncidentCache(String cl, DebugLog il) {
		link = cl;
		inc_log = il;
	}

	/** Put an incident into the cache */
	public void put(ParsedIncident pi) {
		if (pi.isValid()) {
			nxt.add(pi.id);
			if (updated)
				updateIncident(pi);
		} else if (inc_log.isOpen())
			inc_log.log("Invalid incident: " + pi);
	}

	/** Lookup an incident by ID */
	private IncidentImpl lookupIncident(String id) {
		Incident inc = IncidentHelper.lookupOriginal(incidentId(id));
		return (inc instanceof IncidentImpl)
		      ? (IncidentImpl) inc
		      : null;
	}

	/** Get an incident ID */
	private String incidentId(String id) {
		return link + "_" + id;
	}

	/** Update an incident */
	private void updateIncident(ParsedIncident pi) {
		inc_log.log("Updating incident: " + pi);
		Position pos = new Position(pi.lat, pi.lon);
		SphericalMercatorPosition smp =
			SphericalMercatorPosition.convert(pos);
		GeoLoc loc = corridors.snapGeoLoc(smp, LaneType.MAINLINE);
		if (loc != null)
			updateIncident(pi, loc);
		else if (inc_log.isOpen()) {
			inc_log.log("Failed to snap incident to corridor: " +
				pi.lat + ", " + pi.lon);
		}
	}

	/** Update an incident */
	private void updateIncident(ParsedIncident pi, GeoLoc loc) {
		int n_lanes = getLaneCount(LaneType.MAINLINE, loc);
		if (n_lanes > 0)
			updateIncident(pi, loc, n_lanes);
		else if (inc_log.isOpen())
			inc_log.log("No lanes at location: " + loc);
	}

	/** Get the lane count at the incident location */
	private int getLaneCount(LaneType lt, GeoLoc loc) {
		CorridorBase cb = corridors.getCorridor(loc);
		return (cb != null) ? cb.getLaneCount(lt, loc) : 0;
	}

	/** Update an incident */
	private void updateIncident(ParsedIncident pi, GeoLoc loc, int n_lanes){
		IncidentImpl inc = lookupIncident(pi.id);
		if (null == inc && !incidents.contains(pi.id)) {
			createIncidentNotify(incidentId(pi.id), pi, loc,
				IncidentImpact.fromLanes(n_lanes));
		}
	}

	/** Clear old incidents.  Any incidents which have not been refreshed
	 * since this was last called will be cleared. */
	public void clearOld() {
		for (String id : incidents) {
			if (!nxt.contains(id))
				setCleared(id);
		}
		incidents.clear();
		incidents.addAll(nxt);
		nxt.clear();
		updated = true;
	}

	/** Set an incident to cleared status */
	private void setCleared(String id) {
		IncidentImpl inc = lookupIncident(id);
		if (inc != null && !inc.getCleared())
			inc.setClearedNotify(true);
	}
}
