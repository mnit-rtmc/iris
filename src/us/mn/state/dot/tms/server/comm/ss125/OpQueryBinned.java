/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2021  Minnesota Department of Transportation
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
import java.util.Date;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to get binned interval data from a SS125 device
 *
 * @author Douglas Lau
 */
public class OpQueryBinned extends OpSS125 {

	/** Starting pin for controller I/O */
	static private final int START_PIN = 1;

	/** Maximum scan count for occupancy calculation.  Scans are in 16-bit
	 * fixed-point format, with 8-bit integer value (0-100) and 8-bit
	 * fractional part. */
	static private final int MAX_SCANS = 100 << 8;

	/** Binned interval data */
	private final IntervalDataProperty binned_data;

	/** Create a new "query binned" operation */
	public OpQueryBinned(ControllerImpl c, int p) {
		super(PriorityLevel.SHORT_POLL, c);
		binned_data = new IntervalDataProperty(p);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<SS125Property> phaseOne() {
		return new GetCurrentInterval();
	}

	/** Phase to get the most recent interval data */
	private class GetCurrentInterval extends Phase<SS125Property> {

		/** Get the most recent binned interval */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			mess.add(binned_data);
			mess.queryProps();
			return (binned_data.isPreviousInterval())
			      ? null
			      : new SendDateTime();
		}
	}

	/** Phase to send the date and time */
	private class SendDateTime extends Phase<SS125Property> {

		/** Send the date and time */
		protected Phase<SS125Property> poll(
			CommMessage<SS125Property> mess) throws IOException
		{
			long stamp = binned_data.getTime();
			mess.logError("BAD TIMESTAMP: " + new Date(stamp));
			if (!binned_data.isValidStamp())
				binned_data.clear();
			DateTimeProperty date_time = new DateTimeProperty();
			mess.add(date_time);
			mess.storeProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		long stamp = binned_data.getTime();
		int period = binned_data.getPeriod();
		controller.storeVehCount(stamp, period, START_PIN,
			binned_data.getVehCount());
		controller.storeOccupancy(stamp, period, START_PIN,
			binned_data.getScans(), MAX_SCANS);
		controller.storeSpeed(stamp, period, START_PIN,
			binned_data.getSpeed());
		for (SS125VehClass vc: SS125VehClass.values()) {
			controller.storeVehCount(stamp, period, START_PIN,
				binned_data.getVehCount(vc), vc.v_class);
		}
		super.cleanup();
	}
}
