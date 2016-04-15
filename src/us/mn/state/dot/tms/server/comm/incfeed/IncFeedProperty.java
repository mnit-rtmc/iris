/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.IncidentImpact;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;
import static us.mn.state.dot.tms.server.BaseObjectImpl.corridors;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.IncidentCache;
import us.mn.state.dot.tms.server.IncidentImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.utils.LineReader;

/**
 * Incident feed property.
 *
 * @author Douglas Lau
 */
public class IncFeedProperty extends ControllerProperty {

	/** Maximum number of chars in response for line reader */
	static private final int MAX_RESP = 1024;

	/** Incident cache */
	private final IncidentCache cache;

	/** Create a new incident feed property */
	public IncFeedProperty(IncidentCache ic) {
		cache = ic;
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		LineReader lr = new LineReader(is, MAX_RESP);
		String line = lr.readLine();
		while (line != null) {
			ParsedIncident inc = new ParsedIncident(line);
			IncFeedPoller.log("parsed " + inc);
			if (inc.isValid())
				checkIncident(inc);
			line = lr.readLine();
		}
		cache.update();
	}

	/** Check a parsed incident */
	private void checkIncident(ParsedIncident inc) {
		if (cache.contains(inc.id))
			cache.refresh(inc.id);
		else
			cache.put(inc.id, createIncident(inc));
	}

	/** Create an incident */
	private IncidentImpl createIncident(ParsedIncident inc) {
		Position pos = new Position(inc.lat, inc.lon);
		SphericalMercatorPosition smp =
			SphericalMercatorPosition.convert(pos);
		GeoLoc loc = corridors.snapGeoLoc(smp, LaneType.MAINLINE);
		return (loc != null) ? createIncident(inc, loc) : null;
	}

	/** Create an incident */
	private IncidentImpl createIncident(ParsedIncident inc, GeoLoc loc) {
		IncFeedPoller.log("loc: " + GeoLocHelper.getCorridorName(loc));
		int n_lanes = getLaneCount(LaneType.MAINLINE, loc);
		if (n_lanes > 0) {
			Camera cam = lookupCamera(inc);
			return IncidentImpl.createNotify("_" + inc.id,
				inc.inc_type.ordinal(), inc.detail,
				(short) LaneType.MAINLINE.ordinal(),
				loc.getRoadway(), loc.getRoadDir(), inc.lat,
				inc.lon, cam,IncidentImpact.fromLanes(n_lanes));
		} else
			return null;
	}

	/** Get the lane count at the incident location */
	private int getLaneCount(LaneType lt, GeoLoc loc) {
		CorridorBase cb = corridors.getCorridor(loc);
		return (cb != null) ? cb.getLaneCount(lt, loc) : 0;
	}

	/** Lookup the camera */
	private Camera lookupCamera(ParsedIncident inc) {
		Camera cam = CameraHelper.findUID(inc.cam);
		if (cam != null)
			return cam;
		Iterator<Camera> it = CameraHelper.findNearest(
			new Position(inc.lat, inc.lon), 1).iterator();
		return it.hasNext() ? it.next() : null;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "inc.feed";
	}
}
