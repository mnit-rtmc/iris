/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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

import java.io.IOException;
import java.util.Calendar;
import us.mn.state.dot.sched.Job;

/**
 * Job to write out incident XML file.
 *
 * @author Douglas Lau
 */
public class IncidentXmlJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 7;

	/** Create a new incident XML job */
	public IncidentXmlJob() {
		super(Calendar.MINUTE, 1, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the incident XML job */
	public void perform() throws IOException {
		IncidentXmlWriter writer = new IncidentXmlWriter();
		writer.write();
	}
}
