/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;

/**
 * Job to write out XML configuration files.
 *
 * @author Douglas Lau
 */
public class XmlConfigJob extends Job {

	/** TMS config XML writer */
	protected final TmsConfigXmlWriter xml_writer =new TmsConfigXmlWriter();

	/** Create a new XML config writer job */
	public XmlConfigJob() {
		super(Calendar.DATE, 1, Calendar.HOUR, 20);
	}

	/** Create a new one-shot XML config writer job */
	public XmlConfigJob(int ms) {
		super(ms);
	}

	/** Perform the XML config job */
	public void perform() throws IOException {
		System.err.println("Starting XMLDUMP @ " +
			TimeSteward.getDateInstance());
		writeXmlConfiguration();
		System.err.println("Finished XMLDUMP @ " +
			TimeSteward.getDateInstance());
	}

	/** Write the TMS xml configuration files */
	protected void writeXmlConfiguration() throws IOException {
		BaseObjectImpl.corridors.createCorridors();
		xml_writer.writeFragments();
	}
}
