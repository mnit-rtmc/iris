/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ChangeVetoException;
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
		namespace.registerType(SONAR_TYPE, MapExtentImpl.class);
		store.query("SELECT name, lon, lat, zoom FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new MapExtentImpl(
					row.getString(1),	// name
					row.getFloat(2),	// lon
					row.getFloat(3),	// lat
					row.getInt(4)		// zoom
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("lon", lon);
		map.put("lat", lat);
		map.put("zoom", zoom);
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
	public MapExtentImpl(String n) {
		super(n);
	}

	/** Create a map extent */
	protected MapExtentImpl(String n, float ln, float lt, int z) {
		super(n);
		lon = ln;
		lat = lt;
		zoom = z;
	}

	/** Longitude */
	protected float lon;

	/** Set the longitude */
	public void setLon(float ln) {
		lon = ln;
	}

	/** Set the longitude */
	public void doSetLon(float ln) throws TMSException {
		if(ln == lon)
			return;
		store.update(this, "lon", ln);
		setLon(ln);
	}

	/** Get the longitude */
	public float getLon() {
		return lon;
	}

	/** Latitude */
	protected float lat;

	/** Set the latitude */
	public void setLat(float lt) {
		lat = lt;
	}

	/** Set the latitude */
	public void doSetLat(float lt) throws TMSException {
		if(lt == lat)
			return;
		store.update(this, "lat", lt);
		setLat(lt);
	}

	/** Get the latitude */
	public float getLat() {
		return lat;
	}

	/** Zoom level */
	protected int zoom;

	/** Set the zoom level */
	public void setZoom(int z) {
		zoom = z;
	}

	/** Set the zoom level */
	public void doSetZoom(int z) throws TMSException {
		if(z == zoom)
			return;
		if(z < 0 || z > 18)
			throw new ChangeVetoException("Invalid zoom level");
		store.update(this, "zoom", z);
		setZoom(z);
	}

	/** Get the zoom level */
	public int getZoom() {
		return zoom;
	}
}
