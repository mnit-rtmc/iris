/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2013  Minnesota Department of Transportation
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

/**
 * Interface for creating sample archive files.  This allows unit testing of
 * archiving by enabling an alternate implementation for testing.
 *
 * @author Douglas Lau
 */
public interface SampleArchiveFactory {

	/** Create an archive file.
	 * @param sensor_id Sensor identifier.
	 * @param ext File extension.
	 * @param stamp Time stamp.
	 * @return File to archive sample data from that time stamp. */
	File createFile(String sensor_id, String ext, long stamp)
		throws IOException;

	/** Create an archive file.
	 * @param sensor_id Sensor identifier.
	 * @param s_type Periodic sample type.
	 * @param ps Periodic sample to be archived.
	 * @return File to archive periodic sample. */
	File createFile(String sensor_id, PeriodicSampleType s_type,
		PeriodicSample ps) throws IOException;

	/** Test if a sample file name has a known extension */
	boolean hasKnownExtension(String name);
}
