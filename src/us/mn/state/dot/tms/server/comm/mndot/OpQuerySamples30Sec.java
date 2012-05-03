/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query 30-second sample data
 *
 * @author Douglas Lau
 */
public class OpQuerySamples30Sec extends OpQuerySamples {

	/** Sample period (seconds) */
	static private final int SAMPLE_PERIOD_SEC = 30;

	/** Maximum number of scans in 30 seconds */
	static private final int MAX_SCANS = 1800;

	/** Create a new 30-second data operation */
	public OpQuerySamples30Sec(ControllerImpl c, Completer comp) {
		super(PriorityLevel.DATA_30_SEC, c, comp);
	}

	/** Create the first phase of the operation */
	protected Phase phaseOne() {
		return new QuerySample30Sec();
	}

	/** Phase to query the 30-second sample data */
	protected class QuerySample30Sec extends Phase {

		/** Query 30-second sample data */
		protected Phase poll(CommMessage mess) throws IOException {
			byte[] r = new byte[72];
			mess.add(new MemoryProperty(
				Address.DATA_BUFFER_30_SECOND, r));
			mess.queryProps();
			processData(r);
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		long stamp = completer.getStamp();
		controller.storeVolume(stamp, SAMPLE_PERIOD_SEC,
			FIRST_DETECTOR_PIN, volume);
		controller.storeOccupancy(stamp, SAMPLE_PERIOD_SEC,
			FIRST_DETECTOR_PIN, scans, MAX_SCANS);
		super.cleanup();
	}
}
