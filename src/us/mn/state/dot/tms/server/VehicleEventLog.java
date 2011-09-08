/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2011  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;

/**
 * The vehicle event log records vehicle detection events.
 *
 * @author Douglas Lau
 */
public class VehicleEventLog {

	/** Maximum logged headway is 90 seconds */
	static protected final int MAX_HEADWAY = 90 * 1000;

	/** Sample archive factory */
	private final SampleArchiveFactory factory;

	/** Create a new vehicle event log */
	public VehicleEventLog(SampleArchiveFactory f) {
		factory = f;
	}

	/** Log a vehicle detection event */
	public void logVehicle(Calendar stamp, int duration, int headway,
		int speed) throws IOException
	{
		appendEvent(stamp, formatEvent(stamp, duration, headway,speed));
	}

	/** Append an event to the log */
	private void appendEvent(Calendar stamp, String line)
		throws IOException
	{
		File file = factory.createFile(getStampMillis(stamp));
		if(file != null) {
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
	public void logGap() throws IOException {
		p_stamp = null;
		appendEvent(null, "*\n");
	}

	/** Time stamp of most recent vehicle event */
	protected transient Calendar p_stamp;

	/** Format a vehicle detection event */
	protected String formatEvent(Calendar stamp, int duration, int headway,
		int speed)
	{
		boolean log_stamp = false;
		StringBuilder b = new StringBuilder();
		if(duration > 0)
			b.append(duration);
		else
			b.append('?');
		b.append(',');
		if(headway > 0 && headway <= MAX_HEADWAY)
			b.append(headway);
		else {
			b.append('?');
			log_stamp = true;
		}
		if(p_stamp == null || (stamp.get(Calendar.HOUR) !=
			p_stamp.get(Calendar.HOUR)))
		{
			log_stamp = true;
		}
		b.append(',');
		p_stamp = stamp;
		if(log_stamp) {
			if(headway > 0 || duration > 0) {
				long st = stamp.getTimeInMillis();
				b.append(TimeSteward.timeShortString(st));
			} else
				p_stamp = null;
		}
		b.append(',');
		if(speed > 0)
			b.append(speed);
		while(b.charAt(b.length() - 1) == ',')
			b.setLength(b.length() - 1);
		b.append('\n');
		return b.toString();
	}

	/** Get milliseconds for a given timestamp */
	static protected long getStampMillis(Calendar stamp) {
		if(stamp != null)
			return stamp.getTimeInMillis();
		else
			return TimeSteward.currentTimeMillis();
	}
}
