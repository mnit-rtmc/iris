/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2019  Minnesota Department of Transportation
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

import java.io.InputStream;
import java.io.IOException;
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Interval Data Property.
 *
 * @author Douglas Lau
 */
public class IntervalDataProperty extends SS125Property {

	/** Interval period (sec) */
	private final int period;

	/** Get the interval period (sec) */
	public int getPeriod() {
		return period;
	}

	/** Create a new interval data property */
	public IntervalDataProperty(int p) {
		period = p;
	}

	/** Check if time stamp is from the previous interval */
	public boolean isPreviousInterval() {
		long now = TimeSteward.currentTimeMillis();
		int pms = period * 1000;
		long end = now / pms * pms; // end of previous interval
		return (end == stamp);
	}

	/** Is time stamp valid (within valid interval) */
	public boolean isValidStamp() {
		long valid_ms = 2 * period * 1000;
		long now = TimeSteward.currentTimeMillis();
		return (stamp > now - valid_ms) && (stamp < now + valid_ms);
	}

	/** Clear the sample data */
	public void clear() {
		setComplete(false);
		interval = 0;
		stamp = 0;
		n_lanes = 0;
		n_approaches = 0;
		lanes = new LaneInterval[0];
	}

	/** Message ID for interval data request */
	@Override
	protected MessageID msgId() {
		return MessageID.INTERVAL;
	}

	/** Format a QUERY request */
	@Override
	protected byte[] formatQuery() throws IOException {
		byte[] body = new byte[7];
		formatBody(body, MessageType.READ);
		format24(body, 3, interval); // 0 is most recent interval
		return body;
	}

	/** Flag to indicate the request is complete */
	private boolean complete = false;

	/** Test if the request is complete */
	private boolean isComplete() {
		return complete;
	}

	/** Set the complete flag */
	private void setComplete(boolean c) {
		complete = c;
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		while (!isComplete()) {
			super.decodeQuery(c, is);
			msg_sub_id = (byte)(msg_sub_id + 1);
		}
	}

	/** Parse a QUERY response */
	@Override
	protected void parseQuery(byte[] body) throws IOException {
		if (body.length == 6)
			parseResult(body);
		if (body.length != 46)
			throw new ParsingException("BODY LENGTH");
		int n_packet = parse8(body, OFF_MSG_SUB_ID);
		if (n_packet < 0 || n_packet > n_lanes + n_approaches)
			throw new ParsingException("PACKET #");
		if (stamp > 0) {
			if (interval != parse24(body, 3))
				throw new ParsingException("INTERVAL");
			if (stamp != parseDate(body, 6))
				throw new ParsingException("STAMP");
			if (n_lanes != parse8(body, 43))
				throw new ParsingException("# LANES");
			if (n_approaches != parse8(body, 44))
				throw new ParsingException("# APPROACHES");
		} else {
			interval = parse24(body, 3);
			stamp = parseDate(body, 6);
			n_lanes = parse8(body, 43);
			n_approaches = parse8(body, 44);
			lanes = new LaneInterval[n_lanes + n_approaches];
		}
		if (n_packet < lanes.length)
			lanes[n_packet] = new LaneInterval(body);
		if (n_packet + 1 >= n_lanes + n_approaches)
			setComplete(true);
	}

	/** Parse a 16-bit occupancy value.
	 * @param buf Buffer to parse.
	 * @param pos Starting position in buffer.
	 * @return Parsed value. */
	static private int parseOcc(byte[] buf, int pos) {
		int o = parse16(buf, pos);
		// Parsed as unsigned value -- some (undocumented) error codes
		// are returned as negative values.  Treat them as missing data.
		return (o >= 0 && o < Short.MAX_VALUE) ? o : MISSING_DATA;
	}

	/** Interval number */
	private int interval;

	/** Timestamp at end of sample interval */
	private long stamp;

	/** Get timestamp at end of sample interval */
	public long getTime() {
		return stamp;
	}

	/** Number of lanes */
	private int n_lanes;

	/** Number of approaches */
	private int n_approaches;

	/** Lane interval data */
	private LaneInterval[] lanes = new LaneInterval[0];

	/** Test the if property has some data */
	public boolean hasData() {
		return lanes.length > 0;
	}

	/** Lane interval data */
	static public class LaneInterval {
		public final Float speed;
		public final int veh_count;
		public final int scan;
		public final int[] veh_c = new int[SS125VehClass.size];
		public final Float speed_85;
		public final int headway;
		public final int gap;
		private LaneInterval(byte[] body) {
			speed = parse24Fixed(body, 14);
			veh_count = parse24(body, 17);
			scan = parseOcc(body, 20);
			veh_c[0] = parse24(body, 22);
			veh_c[1] = parse24(body, 25);
			veh_c[2] = parse24(body, 28);
			veh_c[3] = parse24(body, 31);
			speed_85 = parse24Fixed(body, 34);
			headway = parse24(body, 37);
			gap = parse24(body, 40);
		}
	}

	/** Get the vehicle count for all lanes */
	public int[] getVehCount() {
		int[] v = new int[lanes.length];
		for (int i = 0; i < v.length; i++) {
			LaneInterval li = lanes[i];
			v[i] = (li != null) ? li.veh_count : MISSING_DATA;
		}
		return v;
	}

	/** Get the vehicle class count for all lanes.
	 * @param vc Vehicle class.
	 * @return Array of vehicle counts, one for each lane. */
	public int[] getVehCount(SS125VehClass vc) {
		int[] v = new int[lanes.length];
		for (int i = 0; i < v.length; i++) {
			LaneInterval li = lanes[i];
			v[i] = (li != null)
			     ? li.veh_c[vc.ordinal()]
			     : MISSING_DATA;
		}
		return v;
	}

	/** Get the scans for all lanes */
	public int[] getScans() {
		int[] scans = new int[lanes.length];
		for (int i = 0; i < scans.length; i++) {
			LaneInterval li = lanes[i];
			scans[i] = (li != null) ? li.scan : MISSING_DATA;
		}
		return scans;
	}

	/** Get the speeds for all lanes */
	public int[] getSpeed() {
		int[] speeds = new int[lanes.length];
		for (int i = 0; i < speeds.length; i++) {
			LaneInterval li = lanes[i];
			speeds[i] = (li != null && li.speed != null)
			          ? Math.round(li.speed)
			          : MISSING_DATA;
		}
		return speeds;
	}

	/** Get the 85th percentile speeds for all lanes */
	public int[] getSpeed85() {
		int[] speeds = new int[lanes.length];
		for (int i = 0; i < speeds.length; i++) {
			LaneInterval li = lanes[i];
			speeds[i] = (li != null && li.speed_85 != null)
			          ? Math.round(li.speed_85)
			          : MISSING_DATA;
		}
		return speeds;
	}

	/** Get the headway for all lanes */
	public int[] getHeadway() {
		int[] headway = new int[lanes.length];
		for (int i = 0; i < headway.length; i++) {
			LaneInterval li = lanes[i];
			headway[i] = (li != null) ? li.headway : MISSING_DATA;
		}
		return headway;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Interval ");
		sb.append(interval);
		sb.append(", ");
		sb.append(new Date(stamp));
		sb.append(", ");
		sb.append(n_lanes);
		sb.append(", ");
		sb.append(n_approaches);
		sb.append(", veh: [");
		for (int v: getVehCount())
			sb.append("" + v + ",");
		sb.setLength(sb.length() - 1);
		sb.append("], scans: [");
		for (int s: getScans())
			sb.append("" + s + ",");
		sb.setLength(sb.length() - 1);
		sb.append("], speed: [");
		for (int s: getSpeed())
			sb.append("" + s + ",");
		sb.setLength(sb.length() - 1);
		sb.append("], speed85: [");
		for (int s: getSpeed85())
			sb.append("" + s + ",");
		sb.setLength(sb.length() - 1);
		sb.append("], headway: [");
		for (int s: getHeadway())
			sb.append("" + s + ",");
		sb.setLength(sb.length() - 1);
		sb.append("]");
		for (SS125VehClass vc: SS125VehClass.values()) {
			sb.append(", ");
			sb.append(vc);
			sb.append(": [");
			for (int v: getVehCount(vc))
				sb.append("" + v + ",");
			sb.setLength(sb.length() - 1);
			sb.append("]");
		}
		return sb.toString();
	}
}
