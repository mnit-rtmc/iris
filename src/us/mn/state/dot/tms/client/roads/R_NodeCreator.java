/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.util.HashMap;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.R_Node;
import static us.mn.state.dot.tms.R_Node.MID_SHIFT;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.geo.Position;

/**
 * This is a utility class to create new r_nodes. It creates both a GeoLoc and
 * an R_Node, because an R_Node cannot have a null GeoLoc.
 *
 * @author Douglas Lau
 */
public class R_NodeCreator implements ProxyListener<GeoLoc> {

	/** The maximum number of new r_node names which can be created
	 * before the TypeCache must be updated.  Creating new r_nodes is
	 * done asynchronously. */
	static private final int MAX_IN_PROCESS_NAMES = 8;

	/** User session */
	private final Session session;

	/** R_Node type cache */
	private final TypeCache<R_Node> r_nodes;

	/** Geo loc type cache */
	private final TypeCache<GeoLoc> geo_locs;

	/** Get the location type cache */
	public TypeCache<GeoLoc> getGeoLocs() {
		return geo_locs;
	}

	/** Unique ID for r_node naming */
	private int uid = 0;

	/** Mapping of names which are in process of being created */
	private HashMap<String, HashMap<String, Object>> in_process =
		new HashMap<String, HashMap<String, Object>>();

	/** Create a new r_node creator */
	public R_NodeCreator(Session s) {
		session = s;
		r_nodes = s.getSonarState().getDetCache().getR_Nodes();
		geo_locs = s.getSonarState().getGeoLocs();
		geo_locs.addProxyListener(this);
	}

	/** Create a new r_node */
	public void create(Road roadway, short road_dir, Position pos,
		int lanes, int shift)
	{
		Float lat = null;
		Float lon = null;
		if (pos != null) {
			lat = (float) pos.getLatitude();
			lon = (float) pos.getLongitude();
		}
		String name = createUniqueR_NodeName();
		if (canAdd(name)) {
			putAttrs(name, lanes, shift);
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			if (roadway != null)
				attrs.put("roadway", roadway);
			attrs.put("road_dir", new Short(road_dir));
			attrs.put("lat", lat);
			attrs.put("lon", lon);
			geo_locs.createObject(name, attrs);
		}
	}

	/** Put a set of attributes for an in-process name */
	private void putAttrs(String name, int lanes, int shift) {
		HashMap<String, Object> attrs =
			new HashMap<String, Object>();
		attrs.put("lanes", lanes);
		attrs.put("shift", shift);
		synchronized (in_process) {
			in_process.put(name, attrs);
		}
	}

	/** Create a new r_node (with no default corridor) */
	public void create(Position pos) {
		create(null, (short)0, pos, 2, MID_SHIFT + 1);
	}

	/** Create a unique R_Node name */
	private String createUniqueR_NodeName() {
		// NOTE: uid needs to persist between calls so that calling
		// this method twice in a row doesn't return the same name
		final int uid_max = r_nodes.size() + MAX_IN_PROCESS_NAMES;
		for (int i = 0; i < uid_max; i++) {
			final int _uid = (uid + i) % uid_max + 1;
			String n = "rnd_" + _uid;
			if (r_nodes.lookupObject(n) == null) {
				uid = _uid;
				return n;
			}
		}
		assert false;
		return null;
	}

	/** Check if the user can add the named r_node */
	public boolean canAdd(String oname) {
		return session.canWrite(GeoLoc.SONAR_TYPE, oname) &&
		       session.canWrite(R_Node.SONAR_TYPE, oname);
	}

	/** Called when a new GeoLoc is added */
	@Override
	public void proxyAdded(GeoLoc loc) {
		String name = loc.getName();
		HashMap<String, Object> attrs = getAttrs(name);
		if (attrs != null) {
			attrs.put("geo_loc", loc);
			r_nodes.createObject(name, attrs);
		}
	}

	/** Get a set of attributes for an in-process name */
	private HashMap<String, Object> getAttrs(String name) {
		synchronized (in_process) {
			return in_process.remove(name);
		}
	}

	/** Called after enumeration is complete */
	@Override
	public void enumerationComplete() {
		// not interested
	}

	/** Called when a GeoLoc is removed */
	@Override
	public void proxyRemoved(GeoLoc loc) {
		// not interested
	}

	/** Called when a GeoLoc attribute changes */
	@Override
	public void proxyChanged(GeoLoc loc, String a) {
		// not interested
	}
}
