/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Interval Data Property.
 *
 * @author Douglas Lau
 */
public class IntervalDataProperty extends SS125Property {

	/** Message ID for interval data request */
	protected MessageID msgId() {
		return MessageID.INTERVAL;
	}

	/** Format a QUERY request */
	protected byte[] formatQuery() throws IOException {
		byte[] body = new byte[7];
		formatBody(body, MessageType.READ);
		format24(body, 3, 0);
		return body;
	}

	/** Flag to indicate the request is complete */
	private boolean complete = false;

	/** Test if the request is complete */
	protected boolean isComplete() {
		return complete;
	}

	/** Set the complete flag */
	protected void setComplete(boolean c) {
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
	protected void parseQuery(byte[] body) throws IOException {
		if(body.length == 6)
			parseResult(body);
		if(body.length != 46)
			throw new ParsingException("BODY LENGTH");
		int n_packet = parse8(body, OFF_MSG_SUB_ID);
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
		public final int scan;
		public final int[] vol_c = new int[SS125VehClass.size];
		public final Float speed_85;
		public final int headway;
		public final int gap;
		protected LaneInterval(byte[] body) {
			speed = parse24Fixed(body, 14);
			volume = parse24(body, 17);
			scan = parse16(body, 20);
			vol_c[0] = parse24(body, 22);
			vol_c[1] = parse24(body, 25);
			vol_c[2] = parse24(body, 28);
			vol_c[3] = parse24(body, 31);
			speed_85 = parse24Fixed(body, 34);
			headway = parse24(body, 37);
			gap = parse24(body, 40);
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
				vol[i] = MISSING_DATA;
		}
		return vol;
	}

	/** Get the vehicle class volume for all lanes.
	 * @param vc Vehicle class.
	 * @return Array of volumes, one for each lane. */
	public int[] getVolume(SS125VehClass vc) {
		int[] vol = new int[lanes.length];
		for(int i = 0; i < vol.length; i++) {
			LaneInterval li = lanes[i];
			if(li != null)
				vol[i] = li.vol_c[vc.ordinal()];
			else
				vol[i] = MISSING_DATA;
		}
		return vol;
	}

	/** Get the scans for all lanes */
	public int[] getScans() {
		int[] scans = new int[lanes.length];
		for(int i = 0; i < scans.length; i++) {
			LaneInterval li = lanes[i];
			if(li != null)
				scans[i] = li.scan;
			else
				scans[i] = MISSING_DATA;
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
				speeds[i] = MISSING_DATA;
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
				speeds[i] = MISSING_DATA;
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
				headway[i] = MISSING_DATA;
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
		sb.setLength(sb.length() - 1);
		sb.append("], speed85: [");
		for(int s: getSpeed85())
			sb.append("" + s + ",");
		sb.setLength(sb.length() - 1);
		sb.append("], headway: [");
		for(int s: getHeadway())
			sb.append("" + s + ",");
		sb.setLength(sb.length() - 1);
		sb.append("]");
		for(SS125VehClass vc: SS125VehClass.values()) {
			sb.append(", ");
			sb.append(vc);
			sb.append(": [");
			for(int v: getVolume(vc))
				sb.append("" + v + ",");
			sb.setLength(sb.length() - 1);
			sb.append("]");
		}
		return sb.toString();
	}
}
