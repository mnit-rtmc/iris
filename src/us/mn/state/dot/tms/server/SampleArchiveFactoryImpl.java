/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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
import java.io.IOException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Factory for creating sample archive files.
 *
 * @author Douglas Lau
 */
public class SampleArchiveFactoryImpl implements SampleArchiveFactory {

	/** Get a valid directory for a given date stamp.
	 * @param stamp Time stamp
	 * @return Directory to store sample data.
	 * @throws IOException If directory cannot be created. */
	static private String directory(long stamp) throws IOException {
		File arc = new File(
			SystemAttrEnum.SAMPLE_ARCHIVE_DIRECTORY.getString(),
			MainServer.districtId());
		if(!arc.exists() && !arc.mkdir())
			throw new IOException("mkdir failed: " + arc);
		String d = TimeSteward.dateShortString(stamp);
		File year = new File(arc, d.substring(0, 4));
		if(!year.exists() && !year.mkdir())
			throw new IOException("mkdir failed: " + year);
		File dir = new File(year.getPath(), d);
		if(!dir.exists() && !dir.mkdir())
			throw new IOException("mkdir failed: " + dir);
		return dir.getCanonicalPath();
	}

	/** File name */
	private final String file_name;

	/** Get a file name for a given period */
	private String getFileName(int period) {
		return file_name + period;
	}

	/** Create a new sample archive factory.
	 * @param s Sensor ID.
	 * @param e File extension. */
	public SampleArchiveFactoryImpl(String s, String e) {
		file_name = s + '.' + e;
	}

	/** Create an archive file.
	 * @param stamp Time stamp at beginning of sample period.
	 * @return File to archive sample data from that time stamp. */
	public File createFile(long stamp) throws IOException {
		return new File(directory(stamp), file_name);
	}

	/** Create an archive file.
	 * @param ps Periodic sample to be archived.
	 * @return File to archive periodic sample. */
	public File createFile(PeriodicSample ps) throws IOException {
		return new File(directory(ps.start()), getFileName(ps.period));
	}
}
