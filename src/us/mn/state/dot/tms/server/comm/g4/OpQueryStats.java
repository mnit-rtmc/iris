/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
 * Copyright (C) 2012  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.g4;

import java.io.IOException;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * This is an operation to query G4 statistics.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpQueryStats extends OpG4 {

	/** Starting pin for controller I/O */
	static private final int START_PIN = 1;

	/** Statistical property */
	private final StatProperty stat;

	/** 30-Second interval completer */
	private final Completer completer;

	/** Time stamp of sample data */
	private final long stamp;

	/** Binning period (seconds) */
	private final int period;

	/** Create a new "query binned samples" operation */
	public OpQueryStats(ControllerImpl c, int p, Completer comp) {
		super(PriorityLevel.DATA_30_SEC, c);
		period = p;
		completer = comp;
		stamp = comp.getStamp();
		stat = new StatProperty(p);
	}

	/** Begin the operation */
	public boolean begin() {
		return completer.beginTask(getKey()) && super.begin();
	}

	/** Create the first phase of the operation */
	protected Phase phaseOne() {
		return new GetCurrentSamples();
	}

	/** Phase to get the most recent binned samples */
	protected class GetCurrentSamples extends Phase {

		/** Get the most recent binned samples */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(stat);
			mess.queryProps();
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		controller.storeVolume(stamp, period, START_PIN,
			stat.getVolume());
		controller.storeOccupancy(stamp, period, START_PIN,
			stat.getScans(), StatProperty.MAX_SCANS);
		controller.storeSpeed(stamp, period, START_PIN,
			stat.getSpeed());
		completer.completeTask(getKey());
		super.cleanup();
	}
}
