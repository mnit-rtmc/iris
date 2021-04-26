/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Active Event Data Property.
 *
 * @author Douglas Lau
 */
public class ActiveEventProperty extends SS125Property {

	/** Valid age of vehicle events (1 hour) */
	static private final long VALID_AGE_MS = 60 * 60 * 1000;

	/** Valid event */
	private boolean valid;

	/** Event timestamp */
	private long stamp;

	/** Lane ID */
	private int lane_id;

	/** Range (ft or m) */
	private float range;

	/** Duration (ms) */
	private int duration;

	/** Speed (mph or kph) */
	private Float speed;

	/** Vehicle classification */
	private int v_class;

	/** Vehicle length (ft or m) */
	private float length;

	/** Get event timestamp */
	public long getTime() {
		return stamp;
	}

	/** Is time stamp valid? */
	public boolean isValidStamp() {
		long now = TimeSteward.currentTimeMillis();
		return (stamp > now - VALID_AGE_MS) && (stamp < now);
	}

	/** Message ID for interval data request */
	@Override
	protected MessageID msgId() {
		return MessageID.ACTIVE_EVENT;
	}

	/** Format a QUERY request */
	@Override
	protected byte[] formatQuery() throws IOException {
		byte[] body = new byte[4];
		formatBody(body, MessageType.READ);
		return body;
	}

	/** Parse a QUERY response */
	@Override
	protected void parseQuery(byte[] body) throws IOException {
		if (body.length == 6)
			parseResult(body);
		if (body.length != 23)
			throw new ParsingException("BODY LENGTH");
		int is_event = parse8(body, OFF_MSG_SUB_ID);
		valid = (is_event == 1);
		stamp = parseDate(body, 3);
		lane_id = parse8(body, 11);
		range = parse16Fixed(body, 12);
		duration = parse24(body, 14);
		speed = parse24Fixed(body, 17);
		v_class = parse8(body, 20);
		length = parse16Fixed(body, 21);
	}

	/** Check if this is a valid event */
	public boolean isValidEvent() {
		return valid;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		if (valid) {
			StringBuilder sb = new StringBuilder();
			sb.append("Vehicle Event,");
			sb.append(new Date(stamp));
			sb.append(",lane=");
			sb.append(lane_id);
			sb.append(",range=");
			sb.append(range);
			sb.append(",duration=");
			sb.append(duration);
			sb.append(",speed=");
			sb.append(speed);
			sb.append(",v_class=");
			sb.append(v_class);
			sb.append(",length=");
			sb.append(length);
			return sb.toString();
		} else
			return "No event";
	}

	/** Log a vehicle detection event */
	public void logVehicle(ControllerImpl controller) {
		DetectorImpl det = controller.getDetectorAtPin(lane_id + 1);
		if (det != null) {
			Calendar cal = TimeSteward.getCalendarInstance();
			cal.setTimeInMillis(stamp);
			int sp = (speed != null) ? Math.round(speed) : 0;
			int len = Math.round(length);
			det.logVehicle(cal, duration, 0, sp, len);
		}
	}
}
