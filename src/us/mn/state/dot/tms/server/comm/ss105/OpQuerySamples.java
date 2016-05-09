/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss105;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.VehLengthClass;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.DownloadRequestException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to get binned samples from a SS105 device
 *
 * @author Douglas Lau
 */
public class OpQuerySamples extends OpSS105 {

	/** Starting pin for controller I/O */
	static private final int START_PIN = 1;

	/** Binning period (seconds) */
	private final int period;

	/** Time stamp of sample data */
	protected long stamp;

	/** Oldest time stamp to accept from controller */
	protected final long oldest;

	/** Newest timestamp to accept from controller */
	protected final long newest;

	/** Binned sample property */
	private final BinnedSampleProperty samples = new BinnedSampleProperty();

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
	protected Phase<SS105Property> phaseOne() {
		return new GetCurrentSamples();
	}

	/** Phase to get the most recent binned samples */
	protected class GetCurrentSamples extends Phase<SS105Property> {

		/** Get the most recent binned samples */
		protected Phase<SS105Property> poll(
			CommMessage<SS105Property> mess) throws IOException
		{
			mess.add(samples);
			mess.queryProps();
			stamp = samples.timestamp.getTime();
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
			samples.getVolume());
		controller.storeOccupancy(stamp, period, START_PIN,
			samples.getScans(), BinnedSampleProperty.MAX_PERCENT);
		controller.storeSpeed(stamp, period, START_PIN,
			samples.getSpeed());
		for (VehLengthClass vc: VehLengthClass.values()) {
			controller.storeVolume(stamp, period, START_PIN,
				samples.getVolume(vc), vc);
		}
		super.cleanup();
	}
}
