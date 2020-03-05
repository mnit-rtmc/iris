/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2019  Minnesota Department of Transportation
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
import java.util.Date;
import us.mn.state.dot.tms.VehLengthClass;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to get binned samples from a SS105 device
 *
 * @author Douglas Lau
 */
public class OpQuerySamples extends OpSS105 {

	/** Starting pin for controller I/O */
	static private final int START_PIN = 1;

	/** Binned sample property */
	private final BinnedSampleProperty sample_data;

	/** Create a new "query binned samples" operation */
	public OpQuerySamples(ControllerImpl c, int p) {
		super(PriorityLevel.DATA_30_SEC, c);
		sample_data = new BinnedSampleProperty(p);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<SS105Property> phaseOne() {
		return new GetCurrentSamples();
	}

	/** Phase to get the most recent binned samples */
	private class GetCurrentSamples extends Phase<SS105Property> {

		/** Get the most recent binned samples */
		protected Phase<SS105Property> poll(
			CommMessage<SS105Property> mess) throws IOException
		{
			mess.add(sample_data);
			mess.queryProps();
			return (sample_data.isPreviousInterval())
			      ? null
			      : new SendDateTime();
		}
	}

	/** Phase to send the date and time */
	private class SendDateTime extends Phase<SS105Property> {

		/** Send the date and time */
		protected Phase<SS105Property> poll(
			CommMessage<SS105Property> mess) throws IOException
		{
			long stamp = sample_data.getTime();
			mess.logError("BAD TIMESTAMP: " + new Date(stamp));
			if (!sample_data.isValidStamp())
				sample_data.clear();
			TimeProperty date_time = new TimeProperty();
			mess.add(date_time);
			mess.storeProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		long stamp = sample_data.getTime();
		int period = sample_data.getPeriod();
		controller.storeVehCount(stamp, period, START_PIN,
			sample_data.getVehCount());
		controller.storeOccupancy(stamp, period, START_PIN,
			sample_data.getScans(), BinnedSampleProperty.MAX_PERCENT);
		controller.storeSpeed(stamp, period, START_PIN,
			sample_data.getSpeed());
		for (VehLengthClass vc: VehLengthClass.values()) {
			controller.storeVehCount(stamp, period, START_PIN,
				sample_data.getVehCount(vc), vc);
		}
		super.cleanup();
	}
}
