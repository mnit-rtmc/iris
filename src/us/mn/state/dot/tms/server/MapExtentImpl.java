/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.MapExtent;
import us.mn.state.dot.tms.TMSException;

/**
 * A map extent is a predefined extent (view) for map toolbar buttons.
 *
 * @author Douglas Lau
 */
public class MapExtentImpl extends BaseObjectImpl implements MapExtent {

	/** Load all the map extents */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading map extents...");
		namespace.registerType(SONAR_TYPE, MapExtentImpl.class);
		store.query("SELECT name, geo_loc FROM iris." + SONAR_TYPE +
			";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new MapExtentImpl(namespace,
					row.getString(1),	// name
					row.getString(2)	// geo_loc
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new map extent */
	public MapExtentImpl(String n) throws SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		MainServer.server.createObject(g);
		geo_loc = g;
	}

	/** Create a map extent */
	protected MapExtentImpl(Namespace ns, String n, String loc) {
		this(n, (GeoLocImpl)ns.lookupObject(GeoLoc.SONAR_TYPE, loc));
	}

	/** Create a map extent */
	protected MapExtentImpl(String n, GeoLocImpl loc) {
		super(n);
		geo_loc = loc;
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		super.doDestroy();
		MainServer.server.removeObject(geo_loc);
	}

	/** Device location */
	protected GeoLocImpl geo_loc;

	/** Get the device location */
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}
}
