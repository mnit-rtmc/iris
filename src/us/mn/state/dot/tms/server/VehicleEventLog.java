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
package us.mn.state.dot.tms.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.SystemAttrEnum;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import static us.mn.state.dot.tms.server.DetectorImpl.SAMPLE_PERIOD_SEC;
import static us.mn.state.dot.tms.server.MainServer.FLUSH;

/**
 * The vehicle event log records vehicle detection events.
 *
 * @author Douglas Lau
 */
public class VehicleEventLog {

	/** Maximum logged headway is 90 seconds */
	static private final int MAX_HEADWAY = 90 * 1000;

	/** Sample period for detectors (ms) */
	static private final int SAMPLE_PERIOD_MS = SAMPLE_PERIOD_SEC * 1000;

	/** Is archiving enabled? */
	static private boolean isArchiveEnabled() {
		return SystemAttrEnum.SAMPLE_ARCHIVE_ENABLE.getBoolean();
	}

	/** Get milliseconds for a given timestamp */
	static private long getStampMillis(Calendar stamp) {
		return (stamp != null)
		      ? stamp.getTimeInMillis()
		      : TimeSteward.currentTimeMillis();
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
	public void logVehicle(final Calendar stamp, final int duration,
		final int headway, final int speed)
	{
		ev_vehicles++;
		ev_duration += duration;
		if (speed > 0) {
			ev_n_speed++;
			ev_speed += speed;
		}
		if (isArchiveEnabled()) {
			FLUSH.addJob(new Job() {
				public void perform() throws IOException {
					appendEvent(stamp, formatEvent(stamp,
						duration, headway, speed));
				}
			});
		}
	}

	/** Append an event to the log */
	private void appendEvent(Calendar stamp, String line)
		throws IOException
	{
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
	public void logGap() {
		p_stamp = null;
		if (isArchiveEnabled()) {
			FLUSH.addJob(new Job() {
				public void perform() throws IOException {
					appendEvent(null, "*\n");
				}
			});
		}
	}

	/** Time stamp of most recent vehicle event */
	private transient Calendar p_stamp;

	/** Format a vehicle detection event */
	private String formatEvent(Calendar stamp, int duration, int headway,
		int speed)
	{
		boolean log_stamp = false;
		StringBuilder b = new StringBuilder();
		if (duration > 0)
			b.append(duration);
		else
			b.append('?');
		b.append(',');
		if (headway > 0 && headway <= MAX_HEADWAY)
			b.append(headway);
		else {
			b.append('?');
			log_stamp = true;
		}
		if (p_stamp == null || (stamp.get(Calendar.HOUR) !=
			p_stamp.get(Calendar.HOUR)))
		{
			log_stamp = true;
		}
		b.append(',');
		p_stamp = stamp;
		if (log_stamp) {
			if (headway > 0 || duration > 0) {
				long st = stamp.getTimeInMillis();
				b.append(TimeSteward.timeShortString(st));
			} else
				p_stamp = null;
		}
		b.append(',');
		if (speed > 0)
			b.append(speed);
		while (b.charAt(b.length() - 1) == ',')
			b.setLength(b.length() - 1);
		b.append('\n');
		return b.toString();
	}

	/** Bin 30-second sample data */
	public void binEventSamples() {
		ev_vehicles = 0;
		ev_duration = 0;
		ev_n_speed = 0;
		ev_speed = 0;
	}

	/** Get the vehicle count */
	public int getVehicleCount() {
		return ev_vehicles;
	}

	/** Get the occupancy for a 30-second period */
	public OccupancySample getOccupancy() {
		return new OccupancySample(0, SAMPLE_PERIOD_SEC, ev_duration,
			SAMPLE_PERIOD_MS);
	}

	/** Calculate the average vehicle speed */
	public int getSpeed() {
		if (ev_n_speed > 0 && ev_speed > 0)
			return ev_speed / ev_n_speed;
		else
			return MISSING_DATA;
	}
}
