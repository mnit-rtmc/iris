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
package us.mn.state.dot.tms.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import us.mn.state.dot.sched.TimeSteward;

/**
 * The event logger logs vehicle detection events
 *
 * @author Douglas Lau
 */
public class EventLogger {

	/** Path where traffic data files are stored */
	static protected final String DATA_PATH = "/var/lib/iris/traffic";

	/** Get a valid directory for a given date stamp */
	static protected File directory(Calendar stamp) throws IOException {
		String d = TimeSteward.dateShortString(stamp.getTimeInMillis());
		File year = new File(DATA_PATH + File.separator +
			d.substring(0, 4));
		if(!year.exists()) {
			if(!year.mkdir())
				throw new IOException("mkdir failed: " + year);
		}
		File dir = new File(year.getPath() + File.separator + d);
		if(!dir.exists()) {
			if(!dir.mkdir())
				throw new IOException("mkdir failed: " + dir);
		}
		return dir;
	}

	/** Create a file (path) for the given time stamp */
	static protected File file(Calendar stamp, String det_id)
		throws IOException
	{
		if(stamp == null)
			stamp = TimeSteward.getCalendarInstance();
		return new File(directory(stamp).getCanonicalPath() +
			File.separator + det_id + ".vlog");
	}

	/** Print a line to the event log file */
	static public void print(Calendar stamp, String det_id, String line)
		throws IOException
	{
		File f = file(stamp, det_id);
		FileWriter fw = new FileWriter(f, true);
		try {
			fw.write(line);
		}
		finally {
			fw.close();
		}
	}
}
