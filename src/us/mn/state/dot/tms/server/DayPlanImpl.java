/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.TMSException;

/**
 * Day plan for scheduling time actions.
 *
 * @author Douglas lau
 */
public class DayPlanImpl extends BaseObjectImpl implements DayPlan {

	/** Load all the day plans */
	static public void loadAll() throws TMSException {
		store.query("SELECT name, holidays FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DayPlanImpl(
					row.getString(1), // name
					row.getBoolean(2) // holidays
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("holidays", holidays);
		return map;
	}

	/** Create a new day plan */
	public DayPlanImpl(String n) {
		super(n);
	}

	/** Create a day plan from database lookup */
	private DayPlanImpl(String n, boolean h) throws TMSException {
		this(n);
		holidays = h;
	}

	/** Holiday value for matchers */
	private boolean holidays;

	/** Get holiday value for matchers */
	@Override
	public boolean getHolidays() {
		return holidays;
	}
}
