/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.IncAdviceHelper;
import us.mn.state.dot.tms.LaneType;
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
		store.query("SELECT name, impact, lane_type, range, " +
			"open_lanes, impacted_lanes, multi FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
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
		map.put("impact", impact);
		map.put("lane_type", lane_type);
		map.put("range", range);
		map.put("open_lanes", open_lanes);
		map.put("impacted_lanes", impacted_lanes);
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

	/** Create an incident advice */
	private IncAdviceImpl(ResultSet row) throws SQLException {
		this(row.getString(1),           // name
		     row.getInt(2),              // impact
		     row.getShort(3),            // lane_type
		     row.getInt(4),              // range
		     (Integer) row.getObject(5), // open_lanes
		     (Integer) row.getObject(6), // impacted_lanes
		     row.getString(7)            // multi
		);
	}

	/** Create an incident advice */
	private IncAdviceImpl(String n, int imp, short lt, int r, Integer oln,
		Integer iln, String m)
	{
		super(n);
		impact = imp;
		lane_type = lt;
		range = r;
		open_lanes = oln;
		impacted_lanes = iln;
		multi = m;
	}

	/** Create a new incident advice */
	public IncAdviceImpl(String n) {
		super(n);
	}

	/** Impact ordinal */
	private int impact;

	/** Set the impact */
	@Override
	public void setImpact(int imp) {
		impact = imp;
	}

	/** Set the impact */
	public void doSetImpact(int imp) throws TMSException {
		if (imp != impact) {
			store.update(this, "impact", imp);
			setImpact(imp);
		}
	}

	/** Get the current impact.
	 * @see us.mn.state.dot.tms.IncImpact */
	@Override
	public int getImpact() {
		return impact;
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

	/** Count of open lanes */
	private Integer open_lanes;

	/** Set count of open lanes */
	@Override
	public void setOpenLanes(Integer oln) {
		open_lanes = oln;
	}

	/** Set count of open lanes */
	public void doSetOpenLanes(Integer oln) throws TMSException {
		if (!objectEquals(oln, open_lanes)) {
			store.update(this, "open_lanes", oln);
			setOpenLanes(oln);
		}
	}

	/** Get count of open lanes */
	@Override
	public Integer getOpenLanes() {
		return open_lanes;
	}

	/** Count of impacted lanes */
	private Integer impacted_lanes;

	/** Set count of impacted lanes */
	@Override
	public void setImpactedLanes(Integer iln) {
		impacted_lanes = iln;
	}

	/** Set count of impacted lanes */
	public void doSetImpactedLanes(Integer iln) throws TMSException {
		if (!objectEquals(iln, impacted_lanes)) {
			store.update(this, "impacted_lanes", iln);
			setImpactedLanes(iln);
		}
	}

	/** Get count of impacted lanes */
	@Override
	public Integer getImpactedLanes() {
		return impacted_lanes;
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
		if (!IncAdviceHelper.isMultiValid(m))
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
