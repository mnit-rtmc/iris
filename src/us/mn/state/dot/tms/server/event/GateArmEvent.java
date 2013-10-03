/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.GateArmState;

/**
 * This is a class for logging gate arm state change events to a database.
 *
 * @author Douglas Lau
 */
public class GateArmEvent extends BaseEvent {

	/** Get corresponding event type for a gate arm state */
	static private EventType gateArmStateEventType(GateArmState gas) {
		switch(gas) {
		case FAULT:
			return EventType.GATE_ARM_FAULT;
		case OPENING:
			return EventType.GATE_ARM_OPENING;
		case OPEN:
			return EventType.GATE_ARM_OPEN;
		case WARN_CLOSE:
			return EventType.GATE_ARM_WARN_CLOSE;
		case CLOSING:
			return EventType.GATE_ARM_CLOSING;
		case CLOSED:
			return EventType.GATE_ARM_CLOSED;
		case TIMEOUT:
			return EventType.GATE_ARM_TIMEOUT;
		default:
			return EventType.GATE_ARM_UNKNOWN;
		}
	}

	/** Device ID (if device specific) */
	private final String device_id;

	/** User who initiated change */
	private final String iris_user;

	/** Create a new gate arm event */
	public GateArmEvent(GateArmState gas, String d, String u) {
		super(gateArmStateEventType(gas));
		device_id = d;
		iris_user = u;
	}

	/** Get the database table name */
	public String getTable() {
		return "event.gate_arm_event";
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_desc_id", event_type.id);
		map.put("event_date", event_date);
		map.put("device_id", device_id);
		map.put("iris_user", iris_user);
		return map;
	}
}
