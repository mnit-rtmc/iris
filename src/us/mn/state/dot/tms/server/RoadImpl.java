/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2019  Minnesota Department of Transportation
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
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.TMSException;

/**
 * The RoadImpl class represents a single road which can be used to logically
 * group traffic management devices.
 *
 * @author Douglas Lau
 */
public class RoadImpl extends BaseObjectImpl implements Road {

	/** Abbreviation regex pattern */
	static private final Pattern ABBREV_PATTERN =
		Pattern.compile("[A-Za-z0-9]{0,6}");

	/** Load all the roads */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, RoadImpl.class);
		store.query("SELECT name, abbrev, r_class, direction " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new RoadImpl(
					row.getString(1), // name
					row.getString(2), // abbrev
					row.getShort(3),  // r_class
					row.getShort(4)   // direction
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("abbrev", abbrev);
		map.put("r_class", r_class);
		map.put("direction", direction);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new road */
	public RoadImpl(String n) {
		super(n);
		abbrev = "";
	}

	/** Create a new road */
	private RoadImpl(String n, String a, short c, short d) {
		super(n);
		abbrev = a;
		r_class = c;
		direction = d;
	}

	/** Abbreviated name (for detector and station names) */
	private String abbrev;

	/** Set the abbreviated name */
	@Override
	public void setAbbrev(String a) {
		abbrev = a;
	}

	/** Set the abbreviated name */
	public void doSetAbbrev(String a) throws TMSException {
		if (a.equals(abbrev))
			return;
		Matcher m = ABBREV_PATTERN.matcher(a);
		if (!m.matches())
			throw new ChangeVetoException("Invalid abbrev: " + a);
		store.update(this, "abbrev", a);
		setAbbrev(a);
	}

	/** Get the abbreviated name */
	@Override
	public String getAbbrev() {
		return abbrev;
	}

	/** Road class */
	private short r_class;

	/** Set the road class */
	@Override
	public void setRClass(short c) {
		r_class = c;
	}

	/** Set the road class */
	public void doSetRClass(short c) throws TMSException {
		if (c != r_class) {
			store.update(this, "r_class", c);
			setRClass(c);
		}
	}

	/** Get the road class */
	@Override
	public short getRClass() {
		return r_class;
	}

	/** Direction (NORTH_SOUTH or EAST_WEST) */
	private short direction;

	/** Set the direction */
	@Override
	public void setDirection(short d) {
		direction = d;
	}

	/** Set the direction */
	public void doSetDirection(short d) throws TMSException {
		if (d == direction)
			return;
		Direction dir = Direction.fromOrdinal(d);
		if (dir != Direction.UNKNOWN &&
		    dir != Direction.NORTH_SOUTH &&
		    dir != Direction.EAST_WEST)
			throw new ChangeVetoException("Invalid direction");
		store.update(this, "direction", d);
		setDirection(d);
	}

	/** Get the direction */
	@Override
	public short getDirection() {
		return direction;
	}
}
