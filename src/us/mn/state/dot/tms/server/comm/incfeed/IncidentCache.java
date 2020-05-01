/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2020  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.IncidentHelper;
import us.mn.state.dot.tms.LaneImpact;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;
import static us.mn.state.dot.tms.server.BaseObjectImpl.corridors;
import us.mn.state.dot.tms.server.IncidentImpl;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;

/**
 * Cache of incidents in an incident feed.
 *
 * @author Douglas Lau
 */
public class IncidentCache {

	/** Maximum distance to snap */
	static private final Distance MAX_DIST = new Distance(0.5, MILES);

	/** Threshold to check if an incident has moved (meters) */
	static private final double MOVE_THRESHOLD_M = 50.0;

	/** Check if an incident has moved */
	static private boolean hasMoved(IncidentImpl inc, ParsedIncident pi) {
		Position p0 = new Position(inc.getLat(), inc.getLon());
		Position p1 = new Position(pi.lat, pi.lon);
		return p0.distanceHaversine(p1) > MOVE_THRESHOLD_M;
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
			if (updated) {
				if (pi.hasLocation())
					updateIncident(pi);
				else
					inc_log.log("No location: " + pi);
			}
		} else if (inc_log.isOpen())
			inc_log.log("Invalid incident: " + pi);
	}

	/** Lookup an incident */
	private IncidentImpl lookupIncident(String id) {
		Incident inc = IncidentHelper.lookupOriginal(originalId(id));
		return (inc instanceof IncidentImpl)
		      ? (IncidentImpl) inc
		      : null;
	}

	/** Get original incident ID */
	private String originalId(String id) {
		return link + "_" + id;
	}

	/** Update an incident */
	private void updateIncident(ParsedIncident pi) {
		Position pos = new Position(pi.lat, pi.lon);
		SphericalMercatorPosition smp =
			SphericalMercatorPosition.convert(pos);
		GeoLoc loc = corridors.snapGeoLoc(smp, LaneType.MAINLINE,
			MAX_DIST, pi.dir);
		if (loc != null)
			updateIncident(pi, loc);
		else if (inc_log.isOpen())
			inc_log.log("Failed to snap incident: " + pi);
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
		String oid = originalId(pi.id);
		// Is this a new incident?
		if (null == inc && !incidents.contains(pi.id)) {
			inc_log.log("Creating incident: " + pi);
			createIncidentNotify(oid, null, pi, loc, n_lanes);
		}
		// Is this a continuing incident?
		if (isContinuing(inc, pi) &&
			(hasMoved(inc, pi) || pi.isDetailChanged(inc)))
		{
			inc_log.log("Updating incident: " + pi);
			inc.setClearedNotify(true);
			inc.notifyRemove();
			String n = IncidentHelper.createUniqueName();
			createIncidentNotify(n, oid, pi, loc, n_lanes);
		}
	}

	/** Check if an incident in continuing */
	private boolean isContinuing(IncidentImpl inc, ParsedIncident pi) {
		return inc != null
		    && incidents.contains(pi.id)
		    && (!inc.getConfirmed())
		    && (!inc.getCleared());
	}

	/** Create an incident and notify clients.
	 * @param n Incident name.
	 * @param orig Original name.
	 * @param pi Parsed incident.
	 * @param loc Geo location.
	 * @param n_lanes Lane count at incident location. */
	private boolean createIncidentNotify(String n, String orig,
		ParsedIncident pi, GeoLoc loc, int n_lanes)
	{
		short lnt = (short) LaneType.MAINLINE.ordinal();
		String im = LaneImpact.fromLanes(n_lanes);
		IncidentImpl inc = new IncidentImpl(n, orig,
			pi.inc_type.id, new Date(), pi.detail,
			lnt, loc.getRoadway(), loc.getRoadDir(),
			pi.lat, pi.lon, pi.lookupCamera(), im, false, false);
		try {
			inc.notifyCreate();
			return true;
		}
		catch (SonarException e) {
			// Probably a cleared incident with the same name
			inc_log.log("Incident not created: " + e.getMessage());
			System.err.println("createIncidentNotify @ " +
				new Date() + ": " + e.getMessage());
			return false;
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
		// Don't automatically clear confirmed incidents
		if (inc != null && !inc.getConfirmed() && !inc.getCleared()) {
			inc.setClearedNotify(true);
			inc_log.log("Incident cleared: " + id);
		}
	}
}
