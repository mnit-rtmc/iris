/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2013  Minnesota Department of Transportation
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
import java.util.Date;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.PeriodicSample;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.DownloadRequestException;
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

	/** Sample of completer */
	private final PeriodicSample comp_sample;

	/** Binning period (seconds) */
	private final int period;

	/** Create a new "query binned samples" operation */
	public OpQueryStats(ControllerImpl c, int p, Completer comp) {
		super(PriorityLevel.DATA_30_SEC, c);
		period = p;
		completer = comp;
		comp_sample = new PeriodicSample(comp.getStamp(), period, 0);
		stat = new StatProperty(p);
	}

	/** Begin the operation */
	public boolean begin() {
		return completer.beginTask(getKey()) && super.begin();
	}

	/** Create the first phase of the operation */
	protected Phase<G4Property> phaseOne() {
		return new GetCurrentSamples();
	}

	/** Phase to get the most recent binned samples */
	protected class GetCurrentSamples extends Phase<G4Property> {

		/** Get the most recent binned samples */
		protected Phase<G4Property> poll(CommMessage<G4Property> mess)
			throws IOException
		{
			mess.add(stat);
			mess.queryProps();
			logQuery(stat);
			long stamp = stat.getStamp();
			PeriodicSample ps = new PeriodicSample(stamp, period,0);
			long e = ps.end();
			if(e < comp_sample.start() || e > comp_sample.end()) {
				logError("BAD TIMESTAMP: " + new Date(stamp));
				setFailed();
				throw new DownloadRequestException(
					controller.toString());
			}
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		long stamp = stat.getStamp();
		controller.storeVolume(stamp, period, START_PIN,
			stat.getVolume());
		controller.storeOccupancy(stamp, period, START_PIN,
			stat.getScans(), StatProperty.MAX_SCANS);
		controller.storeSpeed(stamp, period, START_PIN,
			stat.getSpeed());
		controller.storeVolume(stamp, period, START_PIN,
			stat.getVolume(G4VehClass.SMALL),
			G4VehClass.SMALL.v_class);
		controller.storeVolume(stamp, period, START_PIN,
			stat.getVolume(G4VehClass.REGULAR),
			G4VehClass.REGULAR.v_class);
		controller.storeVolume(stamp, period, START_PIN,
			stat.getVolume(G4VehClass.LARGE),
			G4VehClass.LARGE.v_class);
		controller.storeVolume(stamp, period, START_PIN,
			stat.getVolume(G4VehClass.EXTRA_LARGE),
			G4VehClass.EXTRA_LARGE.v_class);
		completer.completeTask(getKey());
		super.cleanup();
	}
}
