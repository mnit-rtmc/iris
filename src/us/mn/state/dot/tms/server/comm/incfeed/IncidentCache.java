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

import java.util.HashSet;
import java.util.Iterator;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Incident;
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
			if (shouldCreate(pi.id))
				createIncident(pi);
		} else if (inc_log.isOpen())
			inc_log.log("Invalid incident: " + pi);
	}

	/** Check if we should create the incident */
	private boolean shouldCreate(String id) {
		return updated
		    && (!incidents.contains(id))
		    && (lookupIncident(id) == null);
	}

	/** Lookup an incident by ID */
	private IncidentImpl lookupIncident(String id) {
		Incident inc = IncidentHelper.lookup(incidentId(id));
		return (inc instanceof IncidentImpl)
		      ? (IncidentImpl) inc
		      : null;
	}

	/** Get an incident ID */
	private String incidentId(String id) {
		return link + "_" + id;
	}

	/** Create an incident */
	private void createIncident(ParsedIncident pi) {
		inc_log.log("Creating incident: " + pi);
		Position pos = new Position(pi.lat, pi.lon);
		SphericalMercatorPosition smp =
			SphericalMercatorPosition.convert(pos);
		GeoLoc loc = corridors.snapGeoLoc(smp, LaneType.MAINLINE);
		if (loc != null)
			createIncident(pi, loc);
		else if (inc_log.isOpen()) {
			inc_log.log("Failed to snap incident to corridor: " +
				pi.lat + ", " + pi.lon);
		}
	}

	/** Create an incident */
	private void createIncident(ParsedIncident pi, GeoLoc loc) {
		int n_lanes = getLaneCount(LaneType.MAINLINE, loc);
		if (n_lanes > 0)
			createIncident(pi, loc, n_lanes);
		else if (inc_log.isOpen())
			inc_log.log("No lanes at location: " + loc);
	}

	/** Get the lane count at the incident location */
	private int getLaneCount(LaneType lt, GeoLoc loc) {
		CorridorBase cb = corridors.getCorridor(loc);
		return (cb != null) ? cb.getLaneCount(lt, loc) : 0;
	}

	/** Create an incident */
	private void createIncident(ParsedIncident pi, GeoLoc loc, int n_lanes){
		Camera cam = lookupCamera(pi);
		IncidentImpl.createNotify(incidentId(pi.id),
			pi.inc_type.ordinal(), pi.detail,
			(short) LaneType.MAINLINE.ordinal(),
			loc.getRoadway(), loc.getRoadDir(), pi.lat, pi.lon, cam,
			IncidentImpact.fromLanes(n_lanes));
	}

	/** Lookup the camera */
	private Camera lookupCamera(ParsedIncident pi) {
		Camera cam = CameraHelper.findUID(pi.cam);
		if (cam != null)
			return cam;
		Iterator<Camera> it = CameraHelper.findNearest(
			new Position(pi.lat, pi.lon), 1).iterator();
		return it.hasNext() ? it.next() : null;
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
