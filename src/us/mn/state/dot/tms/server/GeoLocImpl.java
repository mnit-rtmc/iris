/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2010  Minnesota Department of Transportation
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

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LocModifier;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.TMSException;

/**
 * GeoLoc contains attributes necessary to describe a map location.
 *
 * @author Douglas Lau
 */
public class GeoLocImpl extends BaseObjectImpl implements GeoLoc {

	/** Load all the geo locations */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading geo locations...");
		namespace.registerType(SONAR_TYPE, GeoLocImpl.class);
		store.query("SELECT name, roadway, road_dir, cross_street, " +
			" cross_dir, cross_mod, easting, northing FROM iris." +
			SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GeoLocImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// roadway
					row.getShort(3),	// road_dir
					row.getString(4),	// cross_street
					row.getShort(5),	// cross_dir
					row.getShort(6),	// cross_mod
					(Integer)row.getObject(7), // easting
					(Integer)row.getObject(8) // northing
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("roadway", roadway);
		map.put("road_dir", road_dir);
		map.put("cross_street", cross_street);
		map.put("cross_dir", cross_dir);
		map.put("cross_mod", cross_mod);
		map.put("easting", easting);
		map.put("northing", northing);
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

	/** Create a new geo location */
	public GeoLocImpl(String n) {
		super(n);
	}

	/** Create a new geo location */
	protected GeoLocImpl(String n, Road r, short rd, Road x, short xd,
		short xm, Integer e, Integer nr)
	{
		this(n);
		roadway = r;
		road_dir = rd;
		cross_street = x;
		cross_dir = xd;
		cross_mod = xm;
		easting = e;
		northing = nr;
	}

	/** Create a new geo location */
	protected GeoLocImpl(Namespace ns, String n, String r, short rd,
		String x, short xd, short xm, Integer e, Integer nr)
	{
		this(n, (Road)ns.lookupObject(Road.SONAR_TYPE, r), rd,
		     (Road)ns.lookupObject(Road.SONAR_TYPE, x), xd, xm, e, nr);
	}

	/** Roadway road */
	protected Road roadway;

	/** Set the roadway road */
	public void setRoadway(Road r) {
		roadway = r;
	}

	/** Set the roadway road */
	public void doSetRoadway(Road r) throws TMSException {
		if(r == roadway)
			return;
		store.update(this, "roadway", r);
		setRoadway(r);
	}

	/** Get the roadway locaiton */
	public Road getRoadway() {
		return roadway;
	}

	/** Roadway direction */
	protected short road_dir;

	/** Set the roadway direction */
	public void setRoadDir(short d) {
		road_dir = d;
	}

	/** Set the roadway direction */
	public void doSetRoadDir(short d) throws TMSException {
		if(d == road_dir)
			return;
		if(!Direction.isValid(d))
			throw new ChangeVetoException("Invalid direction");
		store.update(this, "road_dir", d);
		setRoadDir(d);
	}

	/** Get the roadway direction */
	public short getRoadDir() {
		return road_dir;
	}

	/** Nearest cross-street */
	protected Road cross_street;

	/** Set the cross-street road */
	public void setCrossStreet(Road x) {
		cross_street = x;
	}

	/** Set the cross-street road */
	public void doSetCrossStreet(Road x) throws TMSException {
		if(x == cross_street)
			return;
		store.update(this, "cross_street", x);
		setCrossStreet(x);
	}

	/** Get the cross-street road */
	public Road getCrossStreet() {
		return cross_street;
	}

	/** Cross street direction */
	protected short cross_dir;

	/** Set the cross street direction */
	public void setCrossDir(short d) {
		cross_dir = d;
	}

	/** Set the cross street direction */
	public void doSetCrossDir(short d) throws TMSException {
		if(d == cross_dir)
			return;
		if(!Direction.isValid(d))
			throw new ChangeVetoException("Invalid direction");
		store.update(this, "cross_dir", d);
		setCrossDir(d);
	}

	/** Get the cross street direction */
	public short getCrossDir() {
		return cross_dir;
	}

	/** Cross street modifier */
	protected short cross_mod;

	/** Set the cross street modifier */
	public void setCrossMod(short m) {
		cross_mod = m;
	}

	/** Set the cross street modifier */
	public void doSetCrossMod(short m) throws TMSException {
		if(m == cross_mod)
			return;
		if(!LocModifier.isValid(m))
			throw new ChangeVetoException("Invalid modifier");
		store.update(this, "cross_mod", m);
		setCrossMod(m);
	}

	/** Get the cross street modifier */
	public short getCrossMod() {
		return cross_mod;
	}

	/** UTM Easting */
	protected Integer easting;

	/** Set the UTM Easting */
	public void setEasting(Integer x) {
		easting = x;
	}

	/** Set the UTM Easting */
	public void doSetEasting(Integer x) throws TMSException {
		if(x == easting)
			return;
		if(x != null && x.intValue() < 0)
			throw new ChangeVetoException("Invalid Easting");
		store.update(this, "easting", x);
		setEasting(x);
	}

	/** Get the UTM Easting */
	public Integer getEasting() {
		return easting;
	}

	/** UTM Northing */
	protected Integer northing;

	/** Set the UTM Northing */
	public void setNorthing(Integer y) {
		northing = y;
	}

	/** Set the UTM Northing */
	public void doSetNorthing(Integer y) throws TMSException {
		if(y == northing)
			return;
		if(y != null && y.intValue() < 0)
			throw new ChangeVetoException("Invalid Northing");
		store.update(this, "northing", y);
		setNorthing(y);
	}

	/** Get the UTM Northing */
	public Integer getNorthing() {
		return northing;
	}
}
