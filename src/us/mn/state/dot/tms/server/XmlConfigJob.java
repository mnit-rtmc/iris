/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import java.util.Date;
import us.mn.state.dot.sched.Job;

/**
 * Job to write out XML configuration files.
 *
 * @author Douglas Lau
 */
public class XmlConfigJob extends Job {

	/** Detector XML writer */
	protected final DetectorXmlWriter det_writer = new DetectorXmlWriter();

	/** Ramp meter XML writer */
	protected final RampMeterXmlWriter meter_writer =
		new RampMeterXmlWriter();;

	/** Camera XML writer */
	protected final CameraXmlWriter cam_writer = new CameraXmlWriter();

	/** Geo loc XML writer */
	protected final GeoLocXmlWriter loc_writer = new GeoLocXmlWriter();

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
		System.err.println(new Date().toString() +
			": Writing TMS XML files");
		writeXmlConfiguration();
		System.err.println(new Date().toString() +
			": Completed TMS XML dump");
	}

	/** Write the TMS xml configuration files */
	protected void writeXmlConfiguration() throws IOException {
		det_writer.write();
		BaseObjectImpl.corridors.createCorridors();
		new R_NodeXmlWriter(BaseObjectImpl.corridors).write();
		meter_writer.write();
		cam_writer.write();
		loc_writer.write();
	}
}
