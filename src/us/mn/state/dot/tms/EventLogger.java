/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * The event logger logs vehicle detection events
 *
 * @author Douglas Lau
 */
public class EventLogger {

	/** Path where traffic data files are stored */
	static protected final String DATA_PATH = "/data/traffic";

	/** Create a date string (eg YYYYMMDD) from the given date stamp */
	static public String date(Calendar stamp) {
		StringBuffer b = new StringBuffer(13);
		b.append(stamp.get(Calendar.YEAR));
		while(b.length() < 4)
			b.insert(0, '0');
		b.append(stamp.get(Calendar.MONTH) + 1);
		while(b.length() < 6)
			b.insert(4, '0');
		b.append(stamp.get(Calendar.DAY_OF_MONTH));
		while(b.length() < 8)
			b.insert(6, '0');
		return b.toString();
	}

	/** Get a valid directory for a given date stamp */
	static protected File directory(Calendar stamp) throws IOException {
		String d = date(stamp);
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
			stamp = Calendar.getInstance();
		return new File(directory(stamp).getCanonicalPath() +
			File.separator + det_id + ".vlog");
	}

	/** Print a line to the event log file */
	static public void print(Calendar stamp, String det_id, String line)
		throws IOException
	{
		File f = file(stamp, det_id);
		FileWriter fw = new FileWriter(f, true);
		try { fw.write(line); }
		finally {
			fw.close();
		}
	}
}
