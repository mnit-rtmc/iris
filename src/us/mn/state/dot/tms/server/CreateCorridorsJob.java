/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
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

import java.util.Calendar;
import java.util.Iterator;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;

/**
 * Job to create roadway corridors from R_Nodes
 *
 * @author Douglas Lau
 */
public class CreateCorridorsJob extends Job {

	/** FLUSH Scheduler for writing XML (I/O to disk) */
	private final Scheduler flush;

	/** Create a new create corridors job */
	public CreateCorridorsJob(Scheduler f) {
		super(Calendar.DATE, 1, Calendar.HOUR, 20);
		flush = f;
	}

	/** Perform the job */
	@Override
	public void perform() {
		createCorridors();
		lookupMeterNodes();
		flush.addJob(new XmlConfigJob(1000));
	}

	/** Create the roadway corridors */
	private void createCorridors() {
		CorridorManager cm = BaseObjectImpl.corridors;
		cm.createCorridors();
	}

	/** Lookup associated entrance nodes for all ramp meters */
	private void lookupMeterNodes() {
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while (it.hasNext()) {
			RampMeter m = it.next();
			if (m instanceof RampMeterImpl) {
				RampMeterImpl meter = (RampMeterImpl) m;
				meter.lookupEntranceNode();
			}
		}
	}
}
