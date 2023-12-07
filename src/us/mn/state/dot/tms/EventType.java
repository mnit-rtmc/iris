/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 * This enumeration contains events in the event_description table.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public enum EventType {
	ALARM_TRIGGERED(1), ALARM_CLEARED(2),
	DMS_MSG_ERROR(81), DMS_PIXEL_ERROR(82),
	DMS_DEPLOYED(91), DMS_CLEARED(92),
	DMS_BRIGHT_LOW(101), DMS_BRIGHT_GOOD(102), DMS_BRIGHT_HIGH(103),
	LCS_DEPLOYED(89), LCS_CLEARED(90),
	DET_CHATTER(96), DET_LOCKED_ON(95), DET_NO_HITS(94), DET_NO_CHANGE(97),
	DET_OCC_SPIKE(98),
	QUEUE_DRAINED(10), POLL_TIMEOUT_ERROR(11), PARSING_ERROR(12),
	CHECKSUM_ERROR(13), CONTROLLER_ERROR(14), CONNECTION_REFUSED(15),
	COMM_ERROR(8), COMM_FAILED(65), COMM_RESTORED(9),
	INCIDENT_CLEARED(20), INCIDENT_CRASH(21), INCIDENT_STALL(22),
	INCIDENT_HAZARD(23), INCIDENT_ROADWORK(24), INCIDENT_IMPACT(29),
	CLIENT_CONNECT(201), CLIENT_AUTHENTICATE(202),
	CLIENT_FAIL_AUTHENTICATION(203), CLIENT_DISCONNECT(204),
	CLIENT_CHANGE_PASSWORD(205), CLIENT_FAIL_PASSWORD(206),
	CLIENT_FAIL_DOMAIN(207),
	GATE_ARM_UNKNOWN(301), GATE_ARM_FAULT(302), GATE_ARM_OPENING(303),
	GATE_ARM_OPEN(304), GATE_ARM_WARN_CLOSE(305), GATE_ARM_CLOSING(306),
	GATE_ARM_CLOSED(307),
	METER_EVENT(401),
	BEACON_EVENT(501),
	TAG_READ(601),
	PRICE_DEPLOYED(651), PRICE_VERIFIED(652),
	TT_LINK_TOO_LONG(701), TT_NO_DESTINATION_DATA(703),
	TT_NO_ORIGIN_DATA(704), TT_NO_ROUTE(705),
	CAMERA_SWITCHED(801),
	CAMERA_VIDEO_LOST(811), CAMERA_VIDEO_RESTORED(812),
	ACTION_PLAN_ACTIVATED(900), ACTION_PLAN_DEACTIVATED(901),
	ACTION_PLAN_PHASE_CHANGED(902);

	/** Event type ID */
	public final int id;

	/** Create an event type */
	private EventType(int i) {
		id = i;
	}

	/** Get an event type from the ID */
	static public EventType fromId(int id) {
		for (EventType et: values()) {
			if (et.id == id)
				return et;
		}
		return null;
	}
}
