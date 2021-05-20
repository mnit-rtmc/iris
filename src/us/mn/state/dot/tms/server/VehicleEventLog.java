/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2021  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
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
		return SystemAttrEnum.SAMPLE_ARCHIVE_ENABLE.getBoolean();
	}

	/** Get milliseconds for a given timestamp */
	static private long getStampMillis(long stamp) {
		return (stamp > 0) ? stamp : TimeSteward.currentTimeMillis();
	}

	/** Get the hour for a given timestamp */
	static private int getHour(long stamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(stamp);
		return cal.get(Calendar.HOUR);
	}

	/** Calculate headway if necessary */
	static private int calculateHeadway(int headway, long stamp, long ps) {
		if (headway <= 0 && ps > 0 && stamp > ps)
			headway = (int) (stamp - ps);
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

	/** Count of vehicles in current sampling period */
	private int ev_vehicles = 0;

	/** Total vehicle duration (milliseconds) in current sampling period */
	private int ev_duration = 0;

	/** Count of sampled speed events in current sampling period */
	private int ev_n_speed = 0;

	/** Sum of all vehicle speeds (mph) in current sampling period */
	private int ev_speed = 0;

	/** Create a new vehicle event log */
	public VehicleEventLog(String sid) {
		sensor_id = sid;
		factory = MainServer.a_factory;
	}

	/** Log a vehicle detection event */
	public void logVehicle(final int duration, final int headway,
		final long stamp, final int speed, final int length)
	{
		ev_vehicles++;
		ev_duration += duration;
		if (speed > 0) {
			ev_n_speed++;
			ev_speed += speed;
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
			p_stamp = stamp;
			FLUSH.addJob(new Job() {
				public void perform() throws IOException {
					appendEvent(stamp, ev);
				}
			});
		}
	}

	/** Append an event to the log */
	private void appendEvent(long stamp, String line) throws IOException {
		File file = factory.createFile(sensor_id, "vlog",
			getStampMillis(stamp));
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
		if (isArchiveEnabled()) {
			p_stamp = 0;
			FLUSH.addJob(new Job() {
				public void perform() throws IOException {
					appendEvent(stamp, "*\n");
				}
			});
		}
	}

	/** Time stamp of most recent vehicle event */
	private transient long p_stamp;

	/** Binning flag */
	private transient boolean binning;

	/** Are vehicle events being binned? */
	public boolean isBinning() {
		return binning;
	}

	/** Set flag indicating events are being binned */
	public void setBinning(boolean b) {
		binning = b;
	}

	/** Clear binned counts */
	public void clear() {
		ev_vehicles = 0;
		ev_duration = 0;
		ev_n_speed = 0;
		ev_speed = 0;
	}

	/** Get the vehicle count for a given period */
	public PeriodicSample getVehCount(long stamp, int period) {
		return new PeriodicSample(stamp, period, ev_vehicles);
	}

	/** Get the occupancy for a given period */
	public OccupancySample getOccupancy(long stamp, int period) {
		int per_ms = period * 1000;
		return new OccupancySample(stamp, period, ev_duration, per_ms);
	}

	/** Get the average vehicle speed for a given period */
	public PeriodicSample getSpeed(long stamp, int period) {
		if (ev_n_speed > 0 && ev_speed > 0) {
			int s = ev_speed / ev_n_speed;
			return new PeriodicSample(stamp, period, s);
		}
		return null;
	}
}
