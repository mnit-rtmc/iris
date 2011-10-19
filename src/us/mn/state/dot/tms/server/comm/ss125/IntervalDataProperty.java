/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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
import java.util.Date;
import us.mn.state.dot.tms.Constants;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Interval Data Property.
 *
 * @author Douglas Lau
 */
public class IntervalDataProperty extends SS125Property {

	/** Interval data request ID (from non-volatile memory) */
	static protected final byte MSG_ID_NV = 0x70;

	/** Interval data request ID (from volatile memory) */
	static protected final byte MSG_ID = 0x71;

	/** Maximum scan count */
	static protected final int MAX_SCANS = 1800;

	/** Multiplier to convert occupancy to scan count */
	static protected final float OCC_SCANS = MAX_SCANS / 100f;

	/** Format the body of a GET request */
	byte[] formatBodyGet() throws IOException {
		byte[] body = new byte[6];
		body[0] = MSG_ID;
		body[1] = SUB_ID_DONT_CARE;
		body[2] = REQ_READ;
		format24(0, body, 3);
		return body;
	}

	/** Format the body of a SET request */
	byte[] formatBodySet() throws IOException {
		assert false;
		return null;
	}

	/** Parse the payload of a GET response */
	void parsePayload(byte[] body) throws IOException {
		if(body.length == 5)
			parseResult(body);
		if(body.length != 45)
			throw new ParsingException("BODY LENGTH");
		int n_packet = body[1];
		if(n_packet < 0 || n_packet > n_lanes + n_approaches)
			throw new ParsingException("PACKET #");
		if(stamp > 0) {
			if(interval != parse24(body, 3))
				throw new ParsingException("INTERVAL");
			if(stamp != parseDate(body, 6))
				throw new ParsingException("STAMP");
			if(n_lanes != parse8(body, 43))
				throw new ParsingException("# LANES");
			if(n_approaches != parse8(body, 44))
				throw new ParsingException("# APPROACHES");
		} else {
			interval = parse24(body, 3);
			stamp = parseDate(body, 6);
			n_lanes = parse8(body, 43);
			n_approaches = parse8(body, 44);
			lanes = new LaneInterval[n_lanes + n_approaches];
		}
		if(n_packet < lanes.length)
			lanes[n_packet] = new LaneInterval(body);
		if(n_packet + 1 >= n_lanes + n_approaches)
			setComplete(true);
	}

	/** Interval number */
	protected int interval;

	/** Timestamp at end of sample interval */
	protected long stamp;

	/** Get timestamp at end of sample interval */
	public long getTime() {
		return stamp;
	}

	/** Number of lanes */
	protected int n_lanes;

	/** Number of approaches */
	protected int n_approaches;

	/** Lane interval data */
	protected LaneInterval[] lanes = new LaneInterval[0];

	/** Test the if property has some data */
	public boolean hasData() {
		return lanes.length > 0;
	}

	/** Lane interval data */
	static public class LaneInterval {
		public final Float speed;
		public final int volume;
		public final float occ;
		public final int vol_a;
		public final int vol_b;
		public final int vol_c;
		public final int vol_d;
		public final Float speed_85;
		public final int headway;
		public final int gap;
		protected LaneInterval(byte[] body) {
			speed = parse24Fixed(body, 14);
			volume = parse24(body, 17);
			occ = parse16Fixed(body, 20);
			vol_a = parse24(body, 22);
			vol_b = parse24(body, 25);
			vol_c = parse24(body, 28);
			vol_d = parse24(body, 31);
			speed_85 = parse24Fixed(body, 34);
			headway = parse24(body, 37);
			gap = parse24(body, 40);
		}
		public int getScans() {
			return Math.round(occ * OCC_SCANS);
		}
	}

	/** Get the volume for all lanes */
	public int[] getVolume() {
		int[] vol = new int[lanes.length];
		for(int i = 0; i < vol.length; i++) {
			LaneInterval li = lanes[i];
			if(li != null)
				vol[i] = li.volume;
			else
				vol[i] = Constants.MISSING_DATA;
		}
		return vol;
	}

	/** Get the scans for all lanes */
	public int[] getScans() {
		int[] scans = new int[lanes.length];
		for(int i = 0; i < scans.length; i++) {
			LaneInterval li = lanes[i];
			if(li != null)
				scans[i] = li.getScans();
			else
				scans[i] = Constants.MISSING_DATA;
		}
		return scans;
	}

	/** Get the speeds for all lanes */
	public int[] getSpeed() {
		int[] speeds = new int[lanes.length];
		for(int i = 0; i < speeds.length; i++) {
			LaneInterval li = lanes[i];
			if(li != null && li.speed != null)
				speeds[i] = Math.round(li.speed);
			else
				speeds[i] = Constants.MISSING_DATA;
		}
		return speeds;
	}

	/** Get the 85th percentile speeds for all lanes */
	public int[] getSpeed85() {
		int[] speeds = new int[lanes.length];
		for(int i = 0; i < speeds.length; i++) {
			LaneInterval li = lanes[i];
			if(li != null && li.speed_85 != null)
				speeds[i] = Math.round(li.speed_85);
			else
				speeds[i] = Constants.MISSING_DATA;
		}
		return speeds;
	}

	/** Get the headway for all lanes */
	public int[] getHeadway() {
		int[] headway = new int[lanes.length];
		for(int i = 0; i < headway.length; i++) {
			LaneInterval li = lanes[i];
			if(li != null)
				headway[i] = li.headway;
			else
				headway[i] = Constants.MISSING_DATA;
		}
		return headway;
	}

	/** Get a string representation of the property */
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
		sb.append(", vol: [");
		for(int v: getVolume())
			sb.append("" + v + ",");
		sb.setLength(sb.length() - 1);
		sb.append("], scans: [");
		for(int s: getScans())
			sb.append("" + s + ",");
		sb.setLength(sb.length() - 1);
		sb.append("], speed: [");
		for(int s: getSpeed())
			sb.append("" + s + ",");
		sb.append("], speed85: [");
		for(int s: getSpeed85())
			sb.append("" + s + ",");
		sb.append("], headway: [");
		for(int s: getHeadway())
			sb.append("" + s + ",");
		sb.setLength(sb.length() - 1);
		sb.append("]");
		return sb.toString();
	}
}
