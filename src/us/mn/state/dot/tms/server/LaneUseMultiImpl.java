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
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.TMSException;

/**
 * A lane-use MULTI is an association between lane-use indication and a
 * MULTI string.
 *
 * @author Douglas Lau
 */
public class LaneUseMultiImpl extends BaseObjectImpl implements LaneUseMulti {

	/** Load all the lane-use MULTIs */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading lane-use MULTIs...");
		namespace.registerType(SONAR_TYPE, LaneUseMultiImpl.class);
		store.query("SELECT name, indication, multi FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LaneUseMultiImpl(
					row.getString(1),	// name
					row.getInt(2),		// indication
					row.getString(3)	// multi
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("indication", indication);
		map.put("multi", multi);
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

	/** Create a new lane-use MULTI */
	public LaneUseMultiImpl(String n) {
		super(n);
	}

	/** Create a new lane-use MULTI */
	public LaneUseMultiImpl(String n, int i, String m) {
		this(n);
		indication = i;
		multi = m;
	}

	/** Ordinal of LaneUseIndication */
	protected int indication;

	/** Set the indication (ordinal of LaneUseIndication) */
	public void setIndication(int i) {
		indication = i;
	}

	/** Set the indication (ordinal of LaneUseIndication) */
	public void doSetIndication(int i) throws TMSException {
		if(i == indication)
			return;
		LaneUseIndication ind = LaneUseIndication.fromOrdinal(i);
		if(ind == null)
			throw new ChangeVetoException("Invalid indication:" +i);
		store.update(this, "indication", i);
		setIndication(i);
	}

	/** Get the indication (ordinal of LaneUseIndication) */
	public int getIndication() {
		return indication;
	}

	/** MULTI string associated with the lane-use indication */
	protected String multi;

	/** Set the MULTI string */
	public void setMulti(String m) {
		multi = m;
	}

	/** Set the MULTI string */
	public void doSetMulti(String m) throws TMSException {
		if(m == multi)
			return;
		if(!new MultiString(m).isValid())
			throw new ChangeVetoException("Invalid MULTI: " + m);
		store.update(this, "multi", m);
		setMulti(m);
	}

	/** Get the MULTI string */
	public String getMulti() {
		return multi;
	}
}
