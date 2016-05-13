/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.DownloadRequestException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to get interval samples from a SS125 device
 *
 * @author Douglas Lau
 */
public class OpQuerySamples extends OpSS125 {

	/** Starting pin for controller I/O */
	static private final int START_PIN = 1;

	/** Maximum scan count for occupancy calculation.  Scans are in 16-bit
	 * fixed-point format, with 8-bit integer value (0-100) and 8-bit
	 * fractional part. */
	static private final int MAX_SCANS = 100 << 8;

	/** Binning period (seconds) */
	private final int period;

	/** Time stamp of sample data */
	protected long stamp;

	/** Oldest time stamp to accept from controller */
	protected final long oldest;

	/** Newest timestamp to accept from controller */
	protected final long newest;

	/** Interval sample data */
	protected final IntervalDataProperty sample_data =
		new IntervalDataProperty();

	/** Create a new "query binned samples" operation */
	public OpQuerySamples(ControllerImpl c, int p) {
		super(PriorityLevel.DATA_30_SEC, c);
		period = p;
		stamp = TimeSteward.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(stamp);
		cal.add(Calendar.HOUR, -4);
		oldest = cal.getTimeInMillis();
		cal.setTimeInMillis(stamp);
		cal.add(Calendar.MINUTE, 5);
		newest = cal.getTimeInMillis();
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<SS125Property> phaseOne() {
		return new GetCurrentSamples();
	}

	/** Phase to get the most recent sample interval */
	protected class GetCurrentSamples extends Phase<SS125Property> {

		/** Get the most recent sample interval */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			mess.add(sample_data);
			mess.queryProps();
			stamp = sample_data.getTime();
			if (stamp < oldest || stamp > newest) {
				mess.logError("BAD TIMESTAMP: " +
					new Date(stamp));
				setFailed();
				throw new DownloadRequestException(
					controller.toString());
			}
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		controller.storeVolume(stamp, period, START_PIN,
			sample_data.getVolume());
		controller.storeOccupancy(stamp, period, START_PIN,
			sample_data.getScans(), MAX_SCANS);
		controller.storeSpeed(stamp, period, START_PIN,
			sample_data.getSpeed());
		for (SS125VehClass vc: SS125VehClass.values()) {
			controller.storeVolume(stamp, period, START_PIN,
				sample_data.getVolume(vc), vc.v_class);
		}
		super.cleanup();
	}
}
