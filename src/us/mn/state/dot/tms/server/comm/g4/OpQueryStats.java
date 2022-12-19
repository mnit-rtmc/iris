/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2021  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;
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

	/** Create a new "query binned samples" operation */
	public OpQueryStats(ControllerImpl c, int p) {
		super(PriorityLevel.POLL_HIGH, c);
		stat = new StatProperty(p);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<G4Property> phaseOne() {
		return new GetCurrentSamples();
	}

	/** Phase to get the most recent binned samples */
	private class GetCurrentSamples extends Phase<G4Property> {

		/** Get the most recent binned samples */
		protected Phase<G4Property> poll(CommMessage<G4Property> mess)
			throws IOException
		{
			mess.add(stat);
			mess.queryProps();
			return (stat.isPreviousInterval())
			      ? null
			      : new StoreRTC();
		}
	}

	/** Phase to store the RTC */
	private class StoreRTC extends Phase<G4Property> {

		/** Store the RTC */
		protected Phase<G4Property> poll(
			CommMessage<G4Property> mess) throws IOException
		{
			long stamp = stat.getStamp();
			mess.logError("BAD TIMESTAMP: " + new Date(stamp));
			if (!stat.isValidStamp())
				stat.clear();
			RTCProperty rtc = new RTCProperty();
			rtc.setStamp(TimeSteward.currentTimeMillis());
			mess.add(rtc);
			mess.storeProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		long stamp = stat.getStamp();
		int per_sec = stat.per_sec;
		controller.storeVehCount(stamp, per_sec, START_PIN,
			stat.getVehCount());
		controller.storeOccupancy(stamp, per_sec, START_PIN,
			stat.getScans(), StatProperty.MAX_SCANS);
		controller.storeSpeed(stamp, per_sec, START_PIN,
			stat.getSpeed());
		controller.storeVehCount(stamp, per_sec, START_PIN,
			stat.getVehCount(G4VehClass.SMALL),
			G4VehClass.SMALL.v_class);
		controller.storeVehCount(stamp, per_sec, START_PIN,
			stat.getVehCount(G4VehClass.REGULAR),
			G4VehClass.REGULAR.v_class);
		controller.storeVehCount(stamp, per_sec, START_PIN,
			stat.getVehCount(G4VehClass.LARGE),
			G4VehClass.LARGE.v_class);
		controller.storeVehCount(stamp, per_sec, START_PIN,
			stat.getVehCount(G4VehClass.EXTRA_LARGE),
			G4VehClass.EXTRA_LARGE.v_class);
		super.cleanup();
	}
}
