/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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
import java.util.TreeSet;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DayMatcher;
import us.mn.state.dot.tms.DayMatcherHelper;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.TMSException;

/**
 * Day plan for scheduling time actions.
 *
 * @author Douglas lau
 */
public class DayPlanImpl extends BaseObjectImpl implements DayPlan {

	/** DayPlan / DayMatcher table mapping */
	static private TableMapping mapping;

	/** Load all the day plans */
	static public void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, DayPlanImpl.class);
		mapping = new TableMapping(store, "iris", SONAR_TYPE,
			DayMatcher.SONAR_TYPE);
		store.query("SELECT name FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DayPlanImpl(namespace,
					row.getString(1)	// name
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
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

	/** Create a new day plan */
	public DayPlanImpl(String n) {
		super(n);
	}

	/** Create a day plan from database lookup */
	private DayPlanImpl(Namespace ns, String n) throws TMSException {
		this(n);
		TreeSet<DayMatcherImpl> dm_set = new TreeSet<DayMatcherImpl>();
		for (String o: mapping.lookup(this)) {
			DayMatcher dm = DayMatcherHelper.lookup(o);
			if (dm instanceof DayMatcherImpl)
				dm_set.add((DayMatcherImpl) dm);
		}
		day_matchers = dm_set.toArray(new DayMatcherImpl[0]);
	}

	/** DayMatchers for the day plan */
	private DayMatcherImpl[] day_matchers = new DayMatcherImpl[0];

	/** Set the day matchers assigned to the day plan */
	@Override
	public void setDayMatchers(DayMatcher[] dms) {
		DayMatcherImpl[] _dms = new DayMatcherImpl[dms.length];
		for (int i = 0; i < dms.length; i++) {
			if (dms[i] instanceof DayMatcherImpl)
				_dms[i] = (DayMatcherImpl) dms[i];
		}
		day_matchers = _dms;
	}

	/** Set the day matchers assigned to the day plan */
	public void doSetDayMatchers(DayMatcher[] dms) throws TMSException {
		TreeSet<Storable> dm_set = new TreeSet<Storable>();
		for (DayMatcher dm: dms) {
			if (dm instanceof DayMatcherImpl)
				dm_set.add((DayMatcherImpl) dm);
			else {
				throw new ChangeVetoException(
					"Invalid day matcher");
			}
		}
		mapping.update(this, dm_set);
		setDayMatchers(dms);
	}

	/** Get the day matchers assigned to the day plan */
	@Override
	public DayMatcher[] getDayMatchers() {
		return day_matchers;
	}
}
