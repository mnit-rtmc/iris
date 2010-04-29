/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.comm.ChecksumException;

/**
 * Binary Detection Request
 *
 * @author Douglas Lau
 */
public class BinaryDetectionRequest extends CanogaRequest {

	/** Conversion factor from milliseconds to seconds */
	static protected final int MS_PER_SECOND = 1000;

	/** Conversion factor from seconds to hours */
	static protected final int SEC_PER_HOUR = 3600;

	/** Conversion factor from feet to miles */
	static protected final int FEET_PER_MILE = 5280;

	/** Minimum spacing (feet) between speed pair detectors */
	static protected final int MIN_SPACING = 6;

	/** Minimum duration needed to calculate speed */
	static protected final int MIN_DURATION = 20;

	/** Percent of duration to match vehicle on upstream sensor */
	static protected final float MATCH_DURATION = 0.10f;

	/** Minimum time (ms) elapsed (per foot) between speed events */
	static protected final int MIN_ELAPSED = 5;

	/** Minimum speed (mph) to estimate */
	static protected final int MIN_SPEED = 5;

	/** Maximum speed (mph) to estimate */
	static protected final int MAX_SPEED = 120;

	/** Maximum valid duration (120 seconds) */
	static protected final int MAX_DURATION = 120 * 1000;

	/** Message payload for a GET request */
	static protected final byte[] PAYLOAD_GET = { '*' };

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
		throw new CanogaError("Binary detection is read-only");
	}

	/** Offset for response checksum field */
	static protected final int OFF_CHECKSUM = 36;

	/** Validate a response message */
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

	/** Class for vehicle detection events */
	static protected final class DetectionEvent {

		protected final int duration;	// milliseconds
		protected final int start;	// millisecond stamp
		protected final int count;

		/** Create a new detection event */
		protected DetectionEvent(int d, int s, int c, int st) {
			duration = d;
			start = s;
			count = c;
		}

		/** Test if two detection events are the same */
		public boolean equals(Object o) {
			if(o instanceof DetectionEvent) {
				DetectionEvent other = (DetectionEvent)o;
				return (duration == other.duration) &&
					(start == other.start) &&
					(count == other.count);
			} else
				return false;
		}

		/** Calculate a hash code for the detection event */
		public int hashCode() {
			return (duration << 16) ^ (start ^ count);
		}

		/** Get a string representation of the detection event */
		public String toString() {
			return "duration:" + duration + ",start:" + start +
				",count:" + count;
		}

		/** Check if the Canoga has been reset */
		public boolean is_reset() {
			return (duration == 0) && (start == 0) && (count == 0);
		}

		/** Check for transmission errors in event data */
		public boolean has_errors(DetectionEvent prev) {
			if(prev == null)
				return false;
			if(start == prev.start)
				return !equals(prev);
			if(count == prev.count)
				return !equals(prev);
			if(duration > MAX_DURATION)
				return true;
			return false;
		}

		/** Calculate the time elapsed from t1 to t2 */
		static protected int calculateElapsed(int t1, int t2) {
			long e = t2 - t1;
			if(e < 0)
				e += (1 << 32);
			return (int)e;
		}

		/** Calculate the average elapsed time between events */
		protected float calculateAvgElapsed(DetectionEvent upstream) {
			int e1 = calculateElapsed(upstream.start, start);
			int d = duration - upstream.duration;
			int e2 = e1 + d;
			return (e1 + e2) / 2.0f;
		}

		/** Log the current event in the detection log */
		public void log_event(Calendar stamp, ControllerImpl controller,
			int inp, DetectionEvent prev, int speed)
		{
			int headway = calculateElapsed(prev.start, start);
			int c = prev.count + 1;
			while((c & 0xFF) != count) {
				c += 1;
				headway = 0;
				speed = 0;
				controller.logEvent(stamp, inp + 1, 0, 0, 0);
			}
			controller.logEvent(stamp, inp + 1, duration, headway,
				speed);
		}

		/** Calculate speed (mph) from upstream event/spacing (ft) */
		public int calculateSpeed(DetectionEvent upstream,
			float spacing)
		{
			if(spacing < MIN_SPACING)
				return 0;
			if(duration < MIN_DURATION)
				return 0;
			int d = Math.round(duration * MATCH_DURATION);
			if(upstream.duration < duration - d)
				return 0;
			if(upstream.duration > duration + d)
				return 0;
			float elapsed = calculateAvgElapsed(upstream);
			if(elapsed < MIN_ELAPSED * spacing)
				return 0;
			float fps = spacing * MS_PER_SECOND / elapsed;
			float mph = fps * SEC_PER_HOUR / FEET_PER_MILE;
			if(mph < MIN_SPEED || mph > MAX_SPEED)
				return 0;
			else
				return Math.round(mph);
		}
	}

	/** Previous vehicle detection event data */
	protected final DetectionEvent[] p_events = new DetectionEvent[4];

	/** Current vehicle detection event data */
	protected final DetectionEvent[] c_events = new DetectionEvent[4];

	/** Parse one vehicle detection event */
	static protected DetectionEvent parse_event(byte[] v, int det) {
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

	/** Set the requested value */
	protected void setValue(byte[] v) {
		for(int i = 0; i < 4; i++)
			c_events[i] = parse_event(v, i);
	}

	/** Log a new vehicle detection event */
	protected void logEvents(ControllerImpl controller, Calendar stamp,
		int inp)
	{
		DetectionEvent pe = p_events[inp];
		DetectionEvent ce = c_events[inp];
		if(pe == null || (ce.is_reset() && !ce.equals(pe))) {
			controller.logEvent(null, inp + 1, 0, 0, 0);
			return;
		}
		if(!ce.equals(pe)) {
			int speed = 0;
			int sp = controller.getSpeedPair(inp + 1);
			if(sp > 0 && sp <= 4) {
				DetectorImpl d = controller.getDetectorAtPin(
					inp + 1);
				speed = ce.calculateSpeed(c_events[sp - 1],
					d.getFieldLength());
			}
			ce.log_event(stamp, controller, inp, pe, speed);
		}
	}

	/** Log new vehicle detection events */
	public void logEvents(ControllerImpl controller) {
		Calendar stamp = Calendar.getInstance();
		for(int i = 0; i < 4; i++) {
			if(!c_events[i].has_errors(p_events[i])) {
				logEvents(controller, stamp, i);
				p_events[i] = c_events[i];
			}
		}
	}

	/** Get the requested value */
	public String getValue() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 4; i++) {
			if(i > 0)
				sb.append("\n");
			sb.append("det:");
			sb.append(i);
			sb.append(',');
			sb.append(c_events[i]);
		}
		return sb.toString();
	}
}
