/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;

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
		store.query("SELECT name, freeway, free_dir, cross_street, " +
			" cross_dir, cross_mod, easting, east_off, northing, " +
			"north_off FROM " + SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new GeoLocImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// freeway
					row.getShort(3),	// free_dir
					row.getString(4),	// cross_street
					row.getShort(5),	// cross_dir
					row.getShort(6),	// cross_mod
					(Integer)row.getObject(7), // easting
					(Integer)row.getObject(8), // east_off
					(Integer)row.getObject(9), // northing
					(Integer)row.getObject(10) // north_off
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		if(freeway != null)
			map.put("freeway", freeway.getName());
		map.put("free_dir", free_dir);
		if(cross_street != null)
			map.put("cross_street", cross_street.getName());
		map.put("cross_dir", cross_dir);
		map.put("cross_mod", cross_mod);
		map.put("easting", easting);
		map.put("east_off", east_off);
		map.put("northing", northing);
		map.put("north_off", north_off);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return SONAR_TYPE;
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
	protected GeoLocImpl(String n, Road f, short fd, Road x, short xd,
		short xm, Integer e, Integer eo, Integer nr, Integer no)
	{
		this(n);
		// FIXME: the ancient postgresql driver has a bug which makes
		// a NULL column return 0 for numeric datatypes. This workaround
		// can be removed after upgrading to newer JDBC driver. These
		// fields cannot be 0 anyway, so this trick works in this case.
		if(e == 0)
			e = null;
		if(eo == 0)
			eo = null;
		if(nr == 0)
			nr = null;
		if(no == 0)
			no = null;
		freeway = f;
		free_dir = fd;
		cross_street = x;
		cross_dir = xd;
		cross_mod = xm;
		easting = e;
		east_off = eo;
		northing = nr;
		north_off = no;
	}

	/** Create a new geo location */
	protected GeoLocImpl(Namespace ns, String n, String f, short fd,
		String x, short xd, short xm, Integer e, Integer eo,
		Integer nr, Integer no)
	{
		this(n, (Road)ns.lookupObject(Road.SONAR_TYPE, f), fd,
			(Road)ns.lookupObject(Road.SONAR_TYPE, x), xd, xm,
			e, eo, nr, no);
	}

	/** Freeway road */
	protected Road freeway;

	/** Set the freeway road */
	public void setFreeway(Road f) {
		freeway = f;
	}

	/** Set the freeway road */
	public void doSetFreeway(Road f) throws TMSException {
		if(f == freeway)
			return;
		if(f == null)
			store.update(this, "freeway", null);
		else
			store.update(this, "freeway", f.getName());
		setFreeway(f);
	}

	/** Get the freeway locaiton */
	public Road getFreeway() {
		return freeway;
	}

	/** Freeway direction */
	protected short free_dir;

	/** Set the freeway direction */
	public void setFreeDir(short d) {
		free_dir = d;
	}

	/** Set the freeway direction */
	public void doSetFreeDir(short d) throws TMSException {
		if(d == free_dir)
			return;
		if(d < 0 || d > TMSObject.DIR_FREEWAY.length)
			throw new ChangeVetoException("Invalid direction");
		store.update(this, "free_dir", d);
		setFreeDir(d);
	}

	/** Get the freeway direction */
	public short getFreeDir() {
		return free_dir;
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
		if(x == null)
			store.update(this, "cross_street", null);
		else
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
		if(d < 0 || d > TMSObject.DIRECTION.length)
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
		if(m < 0 || m > TMSObject.MODIFIER.length)
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

	/** UTM Easting offset */
	protected Integer east_off;

	/** Set the UTM Easting offset */
	public void setEastOffset(Integer x) {
		east_off = x;
	}

	/** Set the UTM Easting offset */
	public void doSetEastOffset(Integer x) throws TMSException {
		if(x == east_off)
			return;
		store.update(this, "east_off", x);
		setEastOffset(x);
	}

	/** Get the UTM Easting offset */
	public Integer getEastOffset() {
		return east_off;
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

	/** UTM Northing offset */
	protected Integer north_off;

	/** Set the UTM Northing offset */
	public void setNorthOffset(Integer y) {
		north_off = y;
	}

	/** Set the UTM Northing offset */
	public void doSetNorthOffset(Integer y) throws TMSException {
		if(y == north_off)
			return;
		store.update(this, "north_off", y);
		setNorthOffset(y);
	}

	/** Get the UTM Northing offset */
	public Integer getNorthOffset() {
		return north_off;
	}
}
