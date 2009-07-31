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
		System.err.println("Loading map extents...");
		namespace.registerType(SONAR_TYPE, MapExtentImpl.class);
		store.query("SELECT name, easting, east_span, northing, " +
			"north_span FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new MapExtentImpl(
					row.getString(1),	// name
					row.getInt(2),		// easting
					row.getInt(3),		// east_span
					row.getInt(4),		// northing
					row.getInt(5)		// north_span
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("easting", easting);
		map.put("east_span", east_span);
		map.put("northing", northing);
		map.put("north_span", north_span);
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
	protected MapExtentImpl(String n, int x, int xs, int y, int ys) {
		super(n);
		easting = x;
		east_span = xs;
		northing = y;
		north_span = ys;
	}

	/** UTM Easting */
	protected int easting;

	/** Set the UTM Easting */
	public void setEasting(int x) {
		easting = x;
	}

	/** Set the UTM Easting */
	public void doSetEasting(int x) throws TMSException {
		if(x == easting)
			return;
		if(x < 0)
			throw new ChangeVetoException("Invalid Easting");
		store.update(this, "easting", x);
		setEasting(x);
	}

	/** Get the UTM Easting */
	public int getEasting() {
		return easting;
	}

	/** UTM Easting span */
	protected int east_span;

	/** Set the UTM Easting span */
	public void setEastSpan(int xs) {
		east_span = xs;
	}

	/** Set the UTM Easting span */
	public void doSetEastSpan(int xs) throws TMSException {
		if(xs == east_span)
			return;
		if(xs < 0)
			throw new ChangeVetoException("Invalid Easting span");
		store.update(this, "east_span", xs);
		setEastSpan(xs);
	}

	/** Get the UTM Easting span */
	public int getEastSpan() {
		return east_span;
	}

	/** UTM Northing */
	protected int northing;

	/** Set the UTM Northing */
	public void setNorthing(int y) {
		northing = y;
	}

	/** Set the UTM Northing */
	public void doSetNorthing(int y) throws TMSException {
		if(y == northing)
			return;
		if(y < 0)
			throw new ChangeVetoException("Invalid Northing");
		store.update(this, "northing", y);
		setNorthing(y);
	}

	/** Get the UTM Northing */
	public int getNorthing() {
		return northing;
	}

	/** UTM Northing span */
	protected int north_span;

	/** Set the UTM Northing span */
	public void setNorthSpan(int ys) {
		north_span = ys;
	}

	/** Set the UTM Northing span */
	public void doSetNorthSpan(int ys) throws TMSException {
		if(ys == north_span)
			return;
		store.update(this, "north_span", ys);
		setNorthSpan(ys);
	}

	/** Get the UTM Northing span */
	public int getNorthSpan() {
		return north_span;
	}
}
