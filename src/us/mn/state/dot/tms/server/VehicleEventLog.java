/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.SystemAttrEnum;
import static us.mn.state.dot.tms.server.MainServer.FLUSH;

/**
 * The vehicle event log records vehicle detection events.
 *
 * @author Douglas Lau
 */
public class VehicleEventLog {

	/** Maximum logged headway is 1 hour */
	static private final int MAX_HEADWAY = 60 * 60 * 1000;

	/** Is archiving enabled? */
	static private boolean isArchiveEnabled() {
		return SystemAttrEnum.DETECTOR_DATA_ARCHIVE_ENABLE.getBoolean();
	}

	/** Get milliseconds for a given timestamp */
	static private long getStampMillis(long stamp) {
		return (stamp > 0) ? stamp : TimeSteward.currentTimeMillis();
	}

	/** Get the (local) hour for a given timestamp */
	static private int getHour(long stamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(stamp);
		return cal.get(Calendar.HOUR);
	}

	/** Get the (local) date for a given timestamp */
	static private int getDate(long stamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(stamp);
		return cal.get(Calendar.DATE);
	}

	/** Calculate headway if necessary */
	static private int calculateHeadway(int headway, long stamp, long ps) {
		if (ps > 0 && stamp > ps) {
			if (headway <= 0)
				headway = (int) (stamp - ps);
			else {
				long st = ps + headway;
				// If headway / stamps are more than 1 s off,
				// then headway must be invalid
				if (st < stamp - 1000 || st > stamp + 1000)
					headway = 0;
			}
		}
		return (headway > 0 && headway <= MAX_HEADWAY) ? headway : 0;
	}

	/** Should time stamp be logged */
	static private boolean shouldLogStamp(int headway, long stamp, long ps){
		return (stamp > 0) && (
			(headway <= 0) ||
			(ps <= 0) ||
			(getHour(stamp) != getHour(ps))
		);
	}

	/** Format a vehicle detection event */
	static private String formatEvent(int duration, int headway, long stamp,
		int speed, int length)
	{
		StringBuilder b = new StringBuilder();
		if (duration > 0)
			b.append(duration);
		else
			b.append('?');
		b.append(',');
		if (headway > 0)
			b.append(headway);
		else
			b.append('?');
		b.append(',');
		if (stamp > 0)
			b.append(TimeSteward.timeShortString(stamp));
		b.append(',');
		if (speed > 0)
			b.append(speed);
		b.append(',');
		if (length > 0)
			b.append(length);
		while (b.charAt(b.length() - 1) == ',')
			b.setLength(b.length() - 1);
		b.append('\n');
		return b.toString();
	}

	/** Sample archive factory */
	private final SampleArchiveFactory factory;

	/** Sensor ID */
	private final String sensor_id;

	/** Time stamp of binning period */
	private long bin_stamp = 0;

	/** Count of vehicles in binning period */
	private int bin_vehicles = 0;

	/** Total vehicle duration (milliseconds) in binning period */
	private int bin_duration = 0;

	/** Count of sampled speed events in binning period */
	private int bin_n_speed = 0;

	/** Sum of all vehicle speeds (mph) in binning period */
	private int bin_speed = 0;

	/** Create a new vehicle event log */
	public VehicleEventLog(String sid) {
		sensor_id = sid;
		factory = MainServer.a_factory;
	}

	/** Log a vehicle detection event */
	public void logVehicle(final int duration, final int headway,
		final long stamp, final int speed, final int length)
	{
		if (stamp >= bin_stamp) {
			bin_vehicles++;
			bin_duration += duration;
			if (speed > 0) {
				bin_n_speed++;
				bin_speed += speed;
			}
		}
		if (isArchiveEnabled()) {
			// Check if clock went backwards
			if (stamp > 0 && stamp < p_stamp)
				logGap(stamp);
			int head = calculateHeadway(headway, stamp, p_stamp);
			long st = shouldLogStamp(head, stamp, p_stamp)
			        ? stamp
			        : 0;
			final String ev = formatEvent(duration, head, st, speed,
				length);
			long stamp_ms = getStampMillis(stamp);
			// Are we *inside* a gap and starting a new day?
			if (gap > 0 && getDate(stamp_ms) != getDate(gap)) {
				gap = 0; // new day, new gap
				logGap(stamp_ms);
			}
			p_stamp = stamp;
			gap = 0;
			FLUSH.addJob(new Job() {
				public void perform() throws IOException {
					appendEvent(stamp_ms, ev);
				}
			});
		}
	}

	/** Append an event to the log */
	private void appendEvent(long stamp, String line) throws IOException {
		File file = factory.createFile(sensor_id, "vlog", stamp);
		if (file != null) {
			FileWriter fw = new FileWriter(file, true);
			try {
				fw.write(line);
			}
			finally {
				fw.close();
			}
		}
	}

	/** Log a gap in vehicle events */
	public void logGap(long stamp) {
		long stamp_ms = getStampMillis(stamp);
		if (isArchiveEnabled() && gap == 0) {
			p_stamp = 0;
			gap = stamp_ms;
			FLUSH.addJob(new Job() {
				public void perform() throws IOException {
					appendEvent(stamp_ms, "*\n");
				}
			});
		}
	}

	/** Time stamp of most recent vehicle event */
	private transient long p_stamp = 0;

	/** Time stamp of logging gap (zero for no gap).
	 * Initializing to 1 causes a gap to be logged on IRIS restart. */
	private transient long gap = 1;

	/** Clear binned counts */
	public void clear_bin(long stamp) {
		bin_stamp = stamp;
		bin_vehicles = 0;
		bin_duration = 0;
		bin_n_speed = 0;
		bin_speed = 0;
	}

	/** Get the vehicle count for a given period */
	public PeriodicSample getVehCountSam(long stamp, int per_sec) {
		return new PeriodicSample(stamp, per_sec, bin_vehicles);
	}

	/** Get the occupancy for a given period */
	public OccupancySample getOccupancySam(long stamp, int per_sec) {
		int per_ms = per_sec * 1000;
		return new OccupancySample(stamp, per_sec, bin_duration,
			per_ms);
	}

	/** Get the average vehicle speed for a given period */
	public PeriodicSample getSpeedSam(long stamp, int per_sec) {
		if (bin_n_speed > 0 && bin_speed > 0) {
			int s = bin_speed / bin_n_speed;
			return new PeriodicSample(stamp, per_sec, s);
		}
		return null;
	}
}
