/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Interval Data Request.
 *
 * @author Douglas Lau
 */
public class IntervalDataRequest extends Request {

	/** Interval data request ID (from non-volatile memory) */
	static protected final byte MSG_ID_NV = 0x70;

	/** Interval data request ID (from volatile memory) */
	static protected final byte MSG_ID = 0x71;

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

	/** Date / time stamp */
	protected long stamp;

	/** Get the date / time stamp */
	public long getTime() {
		return stamp;
	}

	/** Number of lanes */
	protected int n_lanes;

	/** Number of approaches */
	protected int n_approaches;

	/** Lane interval data */
	protected LaneInterval[] lanes;

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
	}

	/** Get a string representation of the request */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Interval ");
		sb.append(interval);
		sb.append(", ");
		sb.append(new Date(stamp));
		sb.append(", lanes: ");
		sb.append(n_lanes);
		sb.append(", approaches: ");
		sb.append(n_approaches);
		return sb.toString();
	}
}
