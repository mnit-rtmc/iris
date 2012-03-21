/*
 * IRIS -- Intelligent Roadway Information System
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
 * @author Michael Darter
 */
public class OpQueryStats extends OpG4 {

	/** Bytes read from field controller */
	protected final G4Rec g4_rec = new G4Rec();

	/** 30-Second interval completer */
	protected final Completer completer;

	/** Time stamp of sample data */
	protected long stamp;

	/** Oldest time stamp to accept from controller */
	protected final long oldest = 0;

	/** Newest timestamp to accept from controller */
	protected final long newest = 0;

	/** Volume data for each detector */
	protected int[] volume = new int[LaneSample.MAX_NUM_LANES];

	/** Scan data for each detector */
	protected int[] scans = new int[LaneSample.MAX_NUM_LANES];

	/** Speed data for each detector */
	protected int[] speed = new int[LaneSample.MAX_NUM_LANES];

	/** Create a new "query binned samples" operation */
	public OpQueryStats(ControllerImpl c, Completer comp) {
		super(PriorityLevel.DATA_30_SEC, c);
		G4Poller.info("OpQueryStats("+c+","+comp+") called.");
		completer = comp;
	}

	/** Begin the operation */
	public boolean begin() {
		G4Poller.info("OpQueryStats.begin() called");
		return completer.beginTask(getKey()) && super.begin();
	}

	/** Create the first phase of the operation */
	protected Phase phaseOne() {
		G4Poller.info("OpQueryStats.phaseOne() called.");
		return new GetCurrentSamples();
	}

	/** Phase to get the most recent binned samples */
	protected class GetCurrentSamples extends Phase {

		/** Get the most recent binned samples */
		protected Phase poll(CommMessage mess) throws IOException {
			G4Poller.info("OpQueryStats.poll() called");
			StatProperty bs = 
				new StatProperty(controller, g4_rec);
			mess.add(bs);
			mess.queryProps();
			G4Poller.info("OpQueryStats.GetCurrentSamples." +
				"poll(): success=" + isSuccess());
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		G4Poller.info("OpQueryStats.GetCurrentSamples.cleanup()");
		g4_rec.store(controller);
		completer.completeTask(getKey());
		super.cleanup();
	}
}
