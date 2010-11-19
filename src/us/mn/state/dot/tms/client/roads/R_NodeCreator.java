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
package us.mn.state.dot.tms.client.roads;

import java.util.HashMap;
import java.util.HashSet;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.client.SonarState;

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
	static protected final int MAX_IN_PROCESS_NAMES = 8;

	/** Create a SONAR name to check for adding a geo_loc object */
	static protected Name createGeoLocName(String oname) {
		return new Name(GeoLoc.SONAR_TYPE, oname);
	}

	/** Create a SONAR name to check for adding an r_node object */
	static protected Name createR_NodeName(String oname) {
		return new Name(R_Node.SONAR_TYPE, oname);
	}

	/** SONAR namespace */
	protected final Namespace namespace;

	/** R_Node type cache */
	protected final TypeCache<R_Node> r_nodes;

	/** Get the r_node type cache */
	public TypeCache<R_Node> getR_Nodes() {
		return r_nodes;
	}

	/** Geo loc type cache */
	protected final TypeCache<GeoLoc> geo_locs;

	/** Get the location type cache */
	public TypeCache<GeoLoc> getGeoLocs() {
		return geo_locs;
	}

	/** SONAR User for permission checks */
	protected final User user;

	/** Unique ID for r_node naming */
	protected int uid = 0;

	/** Set of names which are in process of being created */
	protected HashSet<String> in_process = new HashSet<String>();

	/** Create a new r_node creator */
	public R_NodeCreator(SonarState st, User u) {
		namespace = st.getNamespace();
		r_nodes = st.getDetCache().getR_Nodes();
		geo_locs = st.getGeoLocs();
		user = u;
		geo_locs.addProxyListener(this);
	}

	/** Create a new r_node */
	public void create(Road roadway, short road_dir, int easting,
		int northing)
	{
		String name = createUniqueR_NodeName();
		if(canAdd(name)) {
			synchronized(in_process) {
				in_process.add(name);
			}
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			if(roadway != null)
				attrs.put("roadway", roadway);
			attrs.put("road_dir", new Short(road_dir));
			attrs.put("easting", new Integer(easting));
			attrs.put("northing", new Integer(northing));
			geo_locs.createObject(name, attrs);
		}
	}

	/** Create a new r_node (with no default corridor) */
	public void create(int easting, int northing) {
		create(null, (short)0, easting, northing);
	}

	/** Create a unique R_Node name */
	protected String createUniqueR_NodeName() {
		// NOTE: uid needs to persist between calls so that calling
		// this method twice in a row doesn't return the same name
		final int uid_max = r_nodes.size() + MAX_IN_PROCESS_NAMES;
		for(int i = 0; i < uid_max; i++) {
			final int _uid = (uid + i) % uid_max + 1;
			String n = "rnd_" + _uid;
			if(r_nodes.lookupObject(n) == null) {
				uid = _uid;
				return n;
			}
		}
		assert false;
		return null;
	}

	/** Check if the user can add the named r_node */
	public boolean canAdd(String oname) {
		return oname != null &&
			namespace.canAdd(user, createGeoLocName(oname)) &&
			namespace.canAdd(user, createR_NodeName(oname));
	}

	/** Called when a new GeoLoc is added */
	public void proxyAdded(GeoLoc loc) {
		String name = loc.getName();
		synchronized(in_process) {
			if(in_process.contains(name)) {
				HashMap<String, Object> attrs =
					new HashMap<String, Object>();
				attrs.put("geo_loc", loc);
				r_nodes.createObject(name, attrs);
				in_process.remove(name);
			}
		}
	}

	/** Called after enumeration is complete */
	public void enumerationComplete() {
		// not interested
	}

	/** Called when a GeoLoc is removed */
	public void proxyRemoved(GeoLoc loc) {
		// not interested
	}

	/** Called when a GeoLoc attribute changes */
	public void proxyChanged(GeoLoc loc, String a) {
		// not interested
	}
}
