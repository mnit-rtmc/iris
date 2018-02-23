/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Iteris Inc.
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
package us.mn.state.dot.tms.server.event;

import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;

/**
 * Log Action Plan events to the database.
 * @author Michael Darter
 */
public class ActionPlanEvent extends BaseEvent {

	/** Database table name */
	static private final String TABLE = "event.action_plan_event";

	/** Get purge threshold (days) */
	static public int getPurgeDays() {
		return SystemAttrEnum.ACTION_PLAN_EVENT_PURGE_DAYS.getInt();
	}

	/** Purge old records */
	static public void purgeRecords() throws TMSException {
		int age = getPurgeDays();
		if (store != null && age >= 0) {
			store.update("DELETE FROM " + TABLE +
				" WHERE event_date < now() - '" + age +
				" days'::interval;");
		}
	}

	/** Action Plan affected by this event */
	protected final String action_plan;

	/** User who deployed message */
	protected final String iris_user;

	/** Is the specified event an action plan event? */
	static private boolean isActionPlanEvent(EventType et) {
		return et == EventType.ACTION_PLAN_ACTIVATED ||
			et == EventType.ACTION_PLAN_DEACTIVATED;
	}

	/** Create a new event
	 * @arg et Event type
	 * @arg ap Action plan name
	 * @arg iu IRIS user name */
	public ActionPlanEvent(EventType et, String ap, String iu) {
		super(et);
		if (!isActionPlanEvent(et)) {
			assert(false);
		}
		action_plan = ap;
		iris_user = iu;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return TABLE;
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_date", event_date);
		map.put("event_desc_id", event_type.id);
		map.put("action_plan", action_plan);
		map.put("iris_user", iris_user);
		return map;
	}
}
