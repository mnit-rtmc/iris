/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss105;

import java.io.IOException;
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.VehLengthClass;
import us.mn.state.dot.tms.utils.HexString;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Binned Sample Property
 *
 * @author Douglas Lau
 */
public class BinnedSampleProperty extends SS105Property {

	/** Interval period (sec) */
	private final int period;

	/** Get the interval period (sec) */
	public int getPeriod() {
		return period;
	}

	/** Sample age (number of intervals old) */
	private final int age;

	/** Timestamp at end of sample interval */
	private long stamp;

	/** Get timestamp at end of sample interval */
	public long getTime() {
		return stamp;
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
		stamp = 0;
		samples = new LaneSample[0];
	}

	/** Sample data for each lane */
	private LaneSample[] samples = new LaneSample[0];

	/** Create a new binned sample property */
	public BinnedSampleProperty(int p) {
		period = p;
		age = 0;
	}

	/** Check if the property has a checksum */
	protected boolean hasChecksum() {
		return true;
	}

	/** Format a basic "GET" request */
	@Override
	protected String formatGetRequest() {
		if (age < 1)
			return "XD";
		else
			return "XD" + HexString.format(age, 4);
	}

	/** Format a basic "SET" request */
	@Override
	protected String formatSetRequest() {
		return null;
	}

	/** Maximum percentage value of lane sample data */
	static public final int MAX_PERCENT = 1024;

	/** Number of bytes per lane of sample data */
	static protected final int LANE_SAMPLE_BYTES = 29;

	/** Sample data for one lane */
	static public class LaneSample {

		public final int det;
		public final int veh_count;
		public final int speed;		// Miles per Hour
		public final int scans;		// 0-1024 (percentage)
		public final int small;		// 0-1024 (percentage)
		public final int medium;	// 0-1024 (percentage)
		public final int large;		// 0-1024 (percentage)
		public final int[] v_class = new int[VehLengthClass.size];

		protected LaneSample(String s) throws ParsingException {
			det = parseInt(s.substring(0, 1));
			veh_count = parseInt(s.substring(1, 9));
			speed = parseInt(s.substring(9, 13));
			scans = parseInt(s.substring(13, 17));
			small = parseInt(s.substring(17, 21));
			medium = parseInt(s.substring(21, 25));
			large = parseInt(s.substring(25, 29));
			v_class[VehLengthClass.SHORT.ordinal()] =
				vehPercent(small);
			v_class[VehLengthClass.MEDIUM.ordinal()] =
				vehPercent(medium);
			v_class[VehLengthClass.LONG.ordinal()] =
				vehPercent(large);
			v_class[VehLengthClass.MOTORCYCLE.ordinal()] =
				calculateMotorcycleCount();
		}
		static int parseInt(String s) throws ParsingException {
			try {
				return Integer.parseInt(s, 16);
			}
			catch (NumberFormatException e) {
				throw new ParsingException("INVALID SAMPLE");
			}
		}
		public int getScans() {
			return scans;
		}
		static float percent(int i) {
			return 100 * i / (float) MAX_PERCENT;
		}
		private int vehPercent(int i) {
			float p = i / (float) MAX_PERCENT;
			return Math.round(p * veh_count);
		}
		private int calculateMotorcycleCount() {
			int v = veh_count;
			for (VehLengthClass vc: VehLengthClass.values())
				v -= v_class[vc.ordinal()];
			return v >= 0 ? v : MISSING_DATA;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(det);
			sb.append(": ");
			sb.append(veh_count);
			sb.append(", ");
			sb.append(speed);
			sb.append(", ");
			sb.append(percent(scans));
			sb.append("%, ");
			sb.append(small);
			sb.append(", ");
			sb.append(medium);
			sb.append(", ");
			sb.append(large);
			for (VehLengthClass vc: VehLengthClass.values()) {
				sb.append(", ");
				sb.append(vc);
				sb.append(":");
				sb.append(v_class[vc.ordinal()]);
			}
			return sb.toString();
		}
	}

	/** Parse the response to a QUERY */
	@Override
	protected void parseQuery(String r) throws IOException {
		stamp = TimeStamp.parse(r.substring(0, 8)).getTime();
		String payload = r.substring(8);
		if (payload.length() % LANE_SAMPLE_BYTES != 0)
			throw new ParsingException("INVALID SAMPLE SIZE");
		int lanes = payload.length() / LANE_SAMPLE_BYTES;
		samples = buildSamples(lanes, payload);
	}

	/** Build lane sample array */
	private LaneSample[] buildSamples(int lanes, String payload)
		throws IOException
	{
		LaneSample[] s = new LaneSample[lanes];
		for (int i = 0, j = 0; i < lanes; i++) {
			s[i] = new LaneSample(payload.substring(j,
				j + LANE_SAMPLE_BYTES));
			j += LANE_SAMPLE_BYTES;
		}
		return s;
	}

	/** Get a string representation of the sample data */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("XD: ");
		sb.append(new Date(stamp));
		for (LaneSample ls: samples) {
			sb.append('\n');
			sb.append(ls.toString());
		}
		return sb.toString();
	}

	/** Create a set of sample data */
	private int[] createSamples() {
		int[] data = new int[maxDetNumber()];
		for (int i = 0; i < data.length; i++)
			data[i] = MISSING_DATA;
		return data;
	}

	/** Get the highest detector sample number */
	private int maxDetNumber() {
		int dets = 0;
		for (LaneSample ls: samples)
			dets = Math.max(dets, ls.det);
		return dets;
	}

	/** Get the vehicle count array */
	public int[] getVehCount() {
		int[] v = createSamples();
		for (LaneSample ls: samples)
			v[ls.det - 1] = ls.veh_count;
		return v;
	}

	/** Get the vehicle class count for all lanes.
	 * @param vc Vehicle class.
	 * @return Array of vehicle counts, one for each lane. */
	public int[] getVehCount(VehLengthClass vc) {
		int[] v = createSamples();
		for (LaneSample ls: samples)
			v[ls.det - 1] = ls.v_class[vc.ordinal()];
		return v;
	}

	/** Get the scan count array */
	public int[] getScans() {
		int[] scans = createSamples();
		for (LaneSample ls: samples)
			scans[ls.det - 1] = ls.getScans();
		return scans;
	}

	/** Get the speed array */
	public int[] getSpeed() {
		int[] speed = createSamples();
		for (LaneSample ls: samples)
			speed[ls.det - 1] = ls.speed;
		return speed;
	}
}
