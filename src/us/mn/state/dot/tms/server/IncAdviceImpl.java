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
package us.mn.state.dot.tms.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.IncAdvice;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.MultiParser;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.TMSException;

/**
 * An incident advice is part of a message to deploy on a DMS, matching
 * incident attributes.
 *
 * @author Douglas Lau
 */
public class IncAdviceImpl extends BaseObjectImpl implements IncAdvice {

	/** Load all the incident advices */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, IncAdviceImpl.class);
		store.query("SELECT name, sign_group, range, lane_type, " +
			"impact, cleared, multi FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new IncAdviceImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("sign_group", sign_group);
		map.put("range", range);
		map.put("lane_type", lane_type);
		map.put("impact", impact);
		map.put("cleared", cleared);
		map.put("multi", multi);
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

	/** Create a new incident advice */
	private IncAdviceImpl(ResultSet row) throws SQLException {
		this(row.getString(1),		// name
		     row.getString(2),		// sign_group
		     row.getInt(3),		// range
		     row.getShort(4),		// lane_type
		     row.getString(5),		// impact
		     row.getBoolean(6),		// cleared
		     row.getString(7)		// multi
		);
	}

	/** Create a new incident advice */
	private IncAdviceImpl(String n, String sg, int r, short lt, String imp,
		boolean c, String m)
	{
		super(n);
		sign_group = lookupSignGroup(sg);
		range = r;
		lane_type = lt;
		impact = imp;
		cleared = c;
		multi = m;
	}

	/** Create a new incident advice */
	public IncAdviceImpl(String n) {
		super(n);
	}

	/** Sign group */
	private SignGroup sign_group;

	/** Set the sign group */
	@Override
	public void setSignGroup(SignGroup sg) {
		sign_group = sg;
	}

	/** Set the sign group */
	public void doSetSignGroup(SignGroup sg) throws TMSException {
		if (sg != sign_group) {
			store.update(this, "sign_group", sg);
			setSignGroup(sg);
		}
	}

	/** Get the sign group */
	@Override
	public SignGroup getSignGroup() {
		return sign_group;
	}

	/** Range ordinal */
	private int range;

	/** Set the range */
	@Override
	public void setRange(int r) {
		range = r;
	}

	/** Set the range */
	public void doSetRange(int r) throws TMSException {
		if (r != range) {
			store.update(this, "range", r);
			setRange(r);
		}
	}

	/** Get the range */
	@Override
	public int getRange() {
		return range;
	}

	/** Lane type ordinal */
	private short lane_type = (short) LaneType.MAINLINE.ordinal();

	/** Set the lane type ordinal */
	@Override
	public void setLaneType(short lt) {
		lane_type = lt;
	}

	/** Set the lane type ordinal */
	public void doSetLaneType(short lt) throws TMSException {
		checkLaneType(lt);
		if (lt != lane_type) {
			store.update(this, "lane_type", lt);
			setLaneType(lt);
		}
	}

	/** Check for valid lane types */
	private void checkLaneType(short lt) throws ChangeVetoException {
		switch (LaneType.fromOrdinal(lt)) {
		case MAINLINE:
		case EXIT:
		case MERGE:
		case CD_LANE:
			return;
		default:
			throw new ChangeVetoException("INVALID LANE TYPE");
		}
	}

	/** Get the lane type ordinal */
	@Override
	public short getLaneType() {
		return lane_type;
	}

	/** Impact code */
	private String impact = "";

	/** Set the impact code */
	@Override
	public void setImpact(String imp) {
		impact = imp;
	}

	/** Set the impact code */
	public void doSetImpact(String imp) throws TMSException {
		if (!imp.equals(impact)) {
// FIXME:		validateImpact(imp);
			store.update(this, "impact", imp);
			setImpact(imp);
		}
	}

	/** Get the current impact code.
	 * @see us.mn.state.dot.tms.Incident.getImpact() */
	@Override
	public String getImpact() {
		return impact;
	}

	/** Incident cleared status */
	private boolean cleared = false;

	/** Set the cleared status */
	@Override
	public void setCleared(boolean c) {
		cleared = c;
	}

	/** Set the cleared status */
	public void doSetCleared(boolean c) throws TMSException {
		if (c != cleared) {
			store.update(this, "cleared", c);
			setCleared(c);
		}
	}

	/** Get the cleared status */
	@Override
	public boolean getCleared() {
		return cleared;
	}

	/** MULTI string */
	private String multi = "";

	/** Set the MULTI string */
	@Override
	public void setMulti(String m) {
		multi = m;
	}

	/** Set the MULTI string */
	public void doSetMulti(String m) throws TMSException {
		if (!MultiParser.isValid(m))
			throw new ChangeVetoException("Invalid MULTI: " + m);
		if (!m.equals(multi)) {
			store.update(this, "multi", m);
			setMulti(m);
		}
	}

	/** Get the MULTI string */
	@Override
	public String getMulti() {
		return multi;
	}
}
