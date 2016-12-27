/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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

/**
 * This enumeration contains events in the event_description table.
 *
 * @author Douglas Lau
 */
public enum EventType {
	ALARM_TRIGGERED(1), ALARM_CLEARED(2),
	DMS_DEPLOYED(91), DMS_CLEARED(92),
	DMS_BRIGHT_LOW(101), DMS_BRIGHT_GOOD(102), DMS_BRIGHT_HIGH(103),
	LCS_DEPLOYED(89), LCS_CLEARED(90),
	DET_CHATTER(96), DET_LOCKED_ON(95), DET_NO_HITS(94),
	QUEUE_DRAINED(10), POLL_TIMEOUT_ERROR(11), PARSING_ERROR(12),
	CHECKSUM_ERROR(13), CONTROLLER_ERROR(14),
	COMM_ERROR(8), COMM_FAILED(65), COMM_RESTORED(9),
	INCIDENT_CLEARED(20), INCIDENT_CRASH(21), INCIDENT_STALL(22),
	INCIDENT_HAZARD(23), INCIDENT_ROADWORK(24), INCIDENT_IMPACT(29),
	CLIENT_CONNECT(201), CLIENT_AUTHENTICATE(202),
	CLIENT_FAIL_AUTHENTICATION(203), CLIENT_DISCONNECT(204),
	GATE_ARM_UNKNOWN(301), GATE_ARM_FAULT(302), GATE_ARM_OPENING(303),
	GATE_ARM_OPEN(304), GATE_ARM_WARN_CLOSE(305), GATE_ARM_CLOSING(306),
	GATE_ARM_CLOSED(307), GATE_ARM_TIMEOUT(308),
	METER_EVENT(401),
	BEACON_ON_EVENT(501), BEACON_OFF_EVENT(502),
	TAG_READ(601),
	PRICE_DEPLOYED(651), PRICE_VERIFIED(652),
	TT_LINK_TOO_LONG(701), TT_NO_DATA(702), TT_NO_DESTINATION_DATA(703),
	TT_NO_ORIGIN_DATA(704), TT_NO_ROUTE(705);

	/** Event type ID */
	public final int id;

	/** Create an event type */
	private EventType(int i) {
		id = i;
	}

	/** Get an event type from the ID */
	static public EventType fromId(int id) {
		for(EventType et: values()) {
			if(et.id == id)
				return et;
		}
		return null;
	}
}
