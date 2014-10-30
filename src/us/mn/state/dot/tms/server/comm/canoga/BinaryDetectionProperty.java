/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.canoga;

import java.io.IOException;
import java.util.Calendar;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ProtocolException;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.FEET;

/**
 * Binary Detection Property
 *
 * @author Douglas Lau
 */
public class BinaryDetectionProperty extends CanogaProperty {

	/** Minimum time before volume LSB can wrap */
	static private final int VOL_COUNT_WRAP = 4 * 60 * 1000;

	/** Message payload for a GET request */
	static protected final byte[] PAYLOAD_GET = { '*' };

	/** Offset for response checksum field */
	static protected final int OFF_CHECKSUM = 36;

	/** Parse one vehicle detection event */
	static protected DetectionEvent parseEvent(byte[] v, int det) {
		int b = det * 9;
		int duration = ((v[b] & 0xFF) << 16) +
			((v[b + 1] & 0xFF) << 8) +
			(v[b + 2] & 0xFF);
		int start = ((v[b + 3] & 0xFF) << 24) +
			((v[b + 4] & 0xFF) << 16) +
			((v[b + 5] & 0xFF) << 8) +
			(v[b + 6] & 0xFF);
		int count = v[b + 7] & 0xFF;
		int state = v[b + 8] & 0xFF;
		return new DetectionEvent(duration, start, count, state);
	}

	/** Get the expected number of octets in response */
	protected int expectedResponseOctets() {
		return 37;
	}

	/** Format a basic "GET" request */
	protected byte[] formatPayloadGet() {
		return PAYLOAD_GET;
	}

	/** Format a basic "SET" request */
	protected byte[] formatPayloadSet() throws IOException {
		throw new ProtocolException("Binary detection is read-only");
	}

	/** Validate a response message */
	@Override
	protected void validateResponse(byte[] req, byte[] res)
		throws ChecksumException
	{
		byte paysum = res[OFF_CHECKSUM];
		// Clear received checksum for comparison
		res[OFF_CHECKSUM] = 0;
		byte hexsum = checksum(res);
		if(paysum != hexsum)
			throw new ChecksumException(""+ paysum +" != "+ hexsum);
	}

	/** Previous vehicle detection event data */
	protected final DetectionEvent[] p_events = new DetectionEvent[4];

	/** Current vehicle detection event data */
	protected final DetectionEvent[] c_events = new DetectionEvent[4];

	/** Time stamp of most recent event */
	private long event_time = TimeSteward.currentTimeMillis();

	/** Get the number of milliseconds since successful comm */
	private long getEventMillis() {
		return TimeSteward.currentTimeMillis() - event_time;
	}

	/** Set the requested value */
	protected void setValue(byte[] v) {
		if (v.length == expectedResponseOctets()) {
			for (int i = 0; i < 4; i++)
				c_events[i] = parseEvent(v, i);
		}
	}

	/** Log new vehicle detection events */
	public void logEvents(ControllerImpl controller) {
		if (getEventMillis() > VOL_COUNT_WRAP)
			addGap();
		event_time = TimeSteward.currentTimeMillis();
		Calendar stamp = TimeSteward.getCalendarInstance();
		for (int i = 0; i < 4; i++)
			logEvent(controller, stamp, i);
	}

	/** Add a gap in vehicle detection */
	private void addGap() {
		for (int i = 0; i < 4; i++)
			p_events[i] = null;
	}

	/** Log a new vehicle detection event for one input */
	protected void logEvent(ControllerImpl controller, Calendar stamp,
		int inp)
	{
		DetectionEvent pe = p_events[inp];
		DetectionEvent ce = c_events[inp];
		if (ce == null || ce.hasErrors(pe))
			return;
		DetectorImpl det = controller.getDetectorAtPin(inp + 1);
		if (det != null) {
			if ((!ce.isReset()) && (!ce.equals(pe))) {
				int speed = calculateSpeed(controller, inp);
				ce.logEvent(stamp, det, pe, speed);
			}
			p_events[inp] = c_events[inp];
		} else
			p_events[inp] = null;
	}

	/** Calculate the speed from a matching speed loop */
	protected int calculateSpeed(ControllerImpl controller, int inp) {
		int sp = controller.getSpeedPair(inp + 1);
		if (sp > 0 && sp <= 4) {
			DetectorImpl det = controller.getDetectorAtPin(inp + 1);
			if (det != null) {
				DetectionEvent ce = c_events[inp];
				Distance spacing = new Distance(
					det.getFieldLength(), FEET);
				return ce.calculateSpeed(c_events[sp - 1],
					spacing);
			}
		}
		return 0;
	}

	/** Get the property name */
	@Override
	protected String getName() {
		return "Binary Detection";
	}

	/** Get the requested value */
	@Override
	public String getValue() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			if (i > 0)
				sb.append(" ");
			sb.append("det:");
			sb.append(i);
			sb.append(',');
			sb.append(c_events[i]);
		}
		return sb.toString();
	}
}
