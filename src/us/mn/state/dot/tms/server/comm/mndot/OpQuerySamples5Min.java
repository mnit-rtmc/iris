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
import java.util.Calendar;
import java.util.Date;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
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

	/** Maximum number of records to read with "BAD TIMESTAMP" errors */
	static protected final int MAX_BAD_RECORDS = 5;

	/** Time stamp */
	protected long stamp;

	/** Oldest time stamp to accept from controller */
	protected final long oldest;

	/** Newest timestamp to accept from controller */
	protected final long newest;

	/** Count of records with "BAD TIMESTAMP" errors */
	protected int n_bad = 0;

	/** Create a new 5-minute data operation */
	public OpQuerySamples5Min(ControllerImpl c, Completer comp) {
		super(PriorityLevel.DATA_5_MIN, c, comp);
		stamp = comp.getStamp();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(stamp);
		cal.add(Calendar.DATE, -1);
		oldest = cal.getTimeInMillis();
		cal.setTimeInMillis(stamp);
		cal.add(Calendar.MINUTE, 4);
		cal.add(Calendar.SECOND, 20);
		newest = cal.getTimeInMillis();
	}

	/** Create the first phase of the operation */
	protected Phase phaseOne() {
		return new GetNextRecord();
	}

	/** Phase to get the next sample data record */
	protected class GetNextRecord extends Phase {

		/** Binned data record */
		protected byte[] rec;

		/** Try to get and delete the next record */
		protected int tryNextRecord(CommMessage mess)
			throws IOException
		{
			BinnedDataProperty bin = new BinnedDataProperty();
			mess.add(bin);
			mess.queryProps();
			stamp = bin.getStamp();
			rec = bin.getRecord();
			// Delete the record from the controller
			mess.storeProps();
			return bin.getRecordCount();
		}

		/** Test if the timestamp is out of the valid range */
		protected boolean isStampBad() {
			if(stamp < oldest || stamp > newest) {
				MNDOT_LOG.log("BAD TIMESTAMP: " +
					new Date(stamp) + " for " + controller);
				return true;
			} else
				return false;
		}

		/** Collect 5-minute data from the controller */
		protected Phase poll(CommMessage mess) throws IOException {
			int recs = 0;
			try {
				recs = tryNextRecord(mess);
				if(isStampBad()) {
					if(++n_bad > MAX_BAD_RECORDS)
						return null;
					else
						return this;
				}
			}
			catch(ControllerException e) {
				setMaintStatus(e.getMessage());
				rec = new byte[75];
				mess.add(new MemoryProperty(
					Address.DATA_BUFFER_5_MINUTE, rec));
				mess.queryProps();
			}
			processData(rec);
			controller.storeData5Minute(stamp, FIRST_DETECTOR_PIN,
				volume, scans);
			updateGreenCount(meter1,
				rec[Address.OFF_GREEN_METER_1] & 0xFF);
			updateGreenCount(meter2,
				rec[Address.OFF_GREEN_METER_2] & 0xFF);
			if(recs > 0 && TimeSteward.currentTimeMillis() < newest)
				return this;
			else
				return null;
		}
	}

	/** Update meter with the most recent 5-minute green count */
	protected void updateGreenCount(RampMeterImpl meter, int g) {
		if(meter != null) {
			meter.updateGreenCount5(stamp,
				adjustGreenCount(meter, g));
		}
	}
}
