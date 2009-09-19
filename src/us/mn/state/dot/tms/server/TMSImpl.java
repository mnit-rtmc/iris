/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * The TMSImpl class is an RMI object which contains all the global traffic
 * management system object lists
 *
 * @author Douglas Lau
 */
public final class TMSImpl {

	/** Worker thread */
	static protected final Scheduler TIMER =
		new Scheduler("Scheduler: TIMER");

	/** Detector data flush thread */
	static public final Scheduler FLUSH =
		new Scheduler("Scheduler: FLUSH");

	/** Schedule all repeating jobs */
	public void scheduleJobs() {
		int secs = SystemAttrEnum.DMS_POLL_FREQ_SECS.getInt();
		if(secs > 5) {
			TIMER.addJob(new DmsQueryMsgJob(secs));
			TIMER.addJob(new LcsQueryMsgJob(secs));
			TIMER.addJob(new WarnQueryStatusJob(secs));
		}
		TIMER.addJob(new DmsQueryStatusJob());
		TIMER.addJob(new AlarmQueryStatusJob());
		TIMER.addJob(new SampleQuery30SecJob(TIMER));
		TIMER.addJob(new SampleQuery5MinJob(FLUSH));
		TIMER.addJob(new DmsXmlJob());
		TIMER.addJob(new CameraNoFailJob());
		TIMER.addJob(new ProfilingJob());
		TIMER.addJob(new KmlWriterJob());
		TIMER.addJob(new SendSettingsJob());
		TIMER.addJob(new SendSettingsJob(500));
		TIMER.addJob(new XmlConfigJob());
		TIMER.addJob(new XmlConfigJob(1000));
	}
}
