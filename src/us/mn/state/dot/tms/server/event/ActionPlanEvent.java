/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Iteris Inc.
 * Copyright (C) 2018-2024  Minnesota Department of Transportation
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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.TMSException;

/**
 * Log Action Plan events to the database.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class ActionPlanEvent extends BaseEvent {

	/** Is the specified event an action plan event? */
	static private boolean isActionPlanEvent(EventType et) {
		return EventType.ACTION_PLAN_ACTIVATED == et ||
		       EventType.ACTION_PLAN_DEACTIVATED == et ||
		       EventType.ACTION_PLAN_PHASE_CHANGED == et;
	}

	/** Action Plan affected by this event */
	private final String action_plan;

	/** Detail message */
	private final String detail;

	/** Create a new event.
	 * @param et Event type.
	 * @param ap Action plan name.
	 * @param dt Detail message (user name, etc.) */
	public ActionPlanEvent(EventType et, String ap, String dt) {
		super(et);
		assert isActionPlanEvent(et);
		action_plan = ap;
		detail = dt;
	}

	/** Get the event config name */
	@Override
	protected String eventConfigName() {
		return "action_plan_event";
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "event.action_plan_event";
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_date", new Timestamp(event_date.getTime()));
		map.put("event_desc_id", event_type.id);
		map.put("action_plan", action_plan);
		map.put("detail", detail);
		return map;
	}
}
