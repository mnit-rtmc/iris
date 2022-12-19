/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2022  Minnesota Department of Transportation
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
import java.util.Calendar;
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.PeriodicSample;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query 5-minute sample data + meter green counts
 *
 * @author Douglas Lau
 */
public class OpQuerySamples5Min extends OpQuerySamples {

	/** Sample period (seconds) */
	static public final int SAMPLE_PERIOD_SEC = 300;

	/** Maximum number of scans in 5 minutes */
	static private final int MAX_SCANS = 18000;

	/** Maximum number of records to read with "BAD TIMESTAMP" errors */
	static private final int MAX_BAD_RECORDS = 5;

	/** Oldest time stamp to accept from controller */
	private final long oldest;

	/** Newest timestamp to accept from controller */
	private final long newest;

	/** Count of records with "BAD TIMESTAMP" errors */
	protected int n_bad = 0;

	/** Maintenance status */
	private String maint = "";

	/** Create a new 5-minute data operation */
	public OpQuerySamples5Min(ControllerImpl c) {
		super(PriorityLevel.POLL_LOW, c);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getStamp());
		cal.add(Calendar.DATE, -1);
		oldest = cal.getTimeInMillis();
		cal.setTimeInMillis(getStamp());
		cal.add(Calendar.MINUTE, 4);
		cal.add(Calendar.SECOND, 20);
		newest = cal.getTimeInMillis();
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseOne() {
		return new GetNextRecord();
	}

	/** Phase to get the next sample data record */
	protected class GetNextRecord extends Phase<MndotProperty> {

		/** Binned data record */
		private byte[] rec;

		/** Try to get and delete the next record */
		private int tryNextRecord(CommMessage<MndotProperty> mess)
			throws IOException
		{
			BinnedDataProperty bin = new BinnedDataProperty();
			mess.add(bin);
			mess.queryProps();
			setStamp(bin.getStamp());
			rec = bin.getRecord();
			// Delete the record from the controller
			mess.storeProps();
			return bin.getRecordCount();
		}

		/** Test if the timestamp is out of the valid range */
		private boolean isStampBad(long s) {
			return (s < oldest || s > newest);
		}

		/** Collect 5-minute data from the controller */
		@Override
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			int recs = 0;
			try {
				recs = tryNextRecord(mess);
				long s = getStamp();
				if (isStampBad(s)) {
					mess.logError("BAD TIMESTAMP: " +
						new Date(s));
					if (++n_bad > MAX_BAD_RECORDS)
						return null;
					else
						return this;
				}
			}
			catch (ControllerException e) {
				if (!(e instanceof SampleException))
					maint = e.getMessage();
				rec = new byte[75];
				MemoryProperty rec_mem = new MemoryProperty(
					Address.DATA_BUFFER_5_MINUTE, rec);
				mess.add(rec_mem);
				mess.queryProps();
				setStamp();
			}
			processData(rec);
			controller.storeVehCount(getStamp(), SAMPLE_PERIOD_SEC,
				FIRST_DETECTOR_PIN, v_count);
			controller.storeOccupancy(getStamp(), SAMPLE_PERIOD_SEC,
				FIRST_DETECTOR_PIN, scans, MAX_SCANS);
			storeGreenCount(lookupMeter1(controller),
				rec[Address.OFF_GREEN_METER_1] & 0xFF);
			storeGreenCount(lookupMeter2(controller),
				rec[Address.OFF_GREEN_METER_2] & 0xFF);
			if (recs > 0 && TimeSteward.currentTimeMillis() < newest)
				return this;
			else
				return null;
		}
	}

	/** Store green count for a ramp meter */
	private void storeGreenCount(RampMeterImpl meter, int g) {
		if (meter != null) {
			int gc = adjustGreenCount(meter, g);
			DetectorImpl det = meter.getGreenDet();
			if (det != null) {
				long stamp = getStamp();
				PeriodicSample ps = new PeriodicSample(stamp,
					SAMPLE_PERIOD_SEC, gc);
				det.storeVehCount(ps, false);
			}
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			setMaintStatus(maint);
		super.cleanup();
	}
}
